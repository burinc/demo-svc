(ns net.b12n.demo-svc.server
  (:require
   [clojure.java.io
    :as io]
   [clojure.string
    :as str]
   [clojure.tools.logging
    :as log]
   [jsonista.core
    :refer [write-value-as-string]]
   [me.raynes.fs
    :refer [extension]]
   [muuntaja.core
    :as m]
   [net.b12n.demo-svc.core
    :refer [parse-date
            parse-and-validate]]
   [reitit.coercion.spec]
   [reitit.dev.pretty
    :as pretty]
   [reitit.ring
    :as ring]
   [reitit.ring.coercion
    :as coercion]
   [reitit.ring.middleware.exception
    :as exception]
   [reitit.ring.middleware.multipart
    :as multipart]
   [reitit.ring.middleware.muuntaja
    :as muuntaja]
   [reitit.ring.middleware.parameters
    :as parameters]
   [reitit.swagger
    :as swagger]
   [reitit.swagger-ui
    :as swagger-ui]
   [ring.adapter.jetty
    :as jetty])
  (:import
   [java.io
    File]
   [java.net
    URLDecoder])
  (:gen-class))

;; Sample record store, normally should be fetched from database
(def people-db (atom []))

(defn ^:private add-person
  [& [{:keys [last-name
              first-name
              gender
              fav-color
              date-of-birth] :as args}]]
  (swap! people-db
         conj {:first-name    (str/capitalize first-name)
               :last-name     (str/capitalize last-name)
               :gender        (str/capitalize gender)
               :fav-color     (str/capitalize fav-color)
               :date-of-birth date-of-birth}))

(def ^:private support-ext [".csv", ".piped", ".space"])

(defn ^:private support-ext?
  [file]
  (some #{(-> file extension str/lower-case)} support-ext))

#_(support-ext? "test.CsV") ;;=> ".csv"
#_(support-ext? "test.txt") ;;=> nil

(defn file-type
  [file]
  (when-let [ext (support-ext? file)]
    (-> ext
        str/lower-case
        (str/replace "." "")
        keyword)))

;; Note: assume that we don't have giant input file
(defn ^:private load-data!
  "Load data from a given input file to the system.

  Currently support the following file: *.csv, *.piped, and *.space.

  Note: the file extension is used to determine the file type to use by the system.
  Other extension will be ignored."
  [input-file]
  (when-let [input-type (file-type input-file)]
    (doseq [line (-> input-file
                     slurp
                     (str/split-lines))]
      (let [{:keys [last-name
                    first-name
                    gender
                    fav-color
                    date-of-birth
                    err] :as args}
            (parse-and-validate {:type input-type
                                 :data line})]
        (if-not err
          (-> args (dissoc :err) add-person)
          ;; NOTE: we log the validation error, and continue without stopping for now
          ;; TODO: perhaps this should go into the database in real-life application
          (do
            (log/warn (format "Skipping this line as it is not valid input : %s" line))
            (log/warn (format "Reason: %s" err))))))))

(defn sorted-by-gender-then-last-name-asc
  [data]
  (->> data (sort (fn [x y]
                    (let [c (compare (-> x :gender str/lower-case)
                                     (-> y :gender str/lower-case))]
                      (if-not (zero? c)
                        c
                        (compare (:last-name x)
                                 (:last-name y))))))))

(defn sorted-by-first-name-asc
  [data]
  (->> data (sort-by :first-name)))

(defn sorted-by-birth-date-asc
  [data]
  (->> data (sort-by (fn [x] (-> x :date-of-birth parse-date)))))

(defn sorted-by-last-name-dsc
  [data]
  (->> data (sort-by :last-name (fn [x y] (compare y x)))))

(defn all-data-handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> @people-db write-value-as-string)})

(defn gender-handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> @people-db
             sorted-by-gender-then-last-name-asc
             write-value-as-string)})

(defn birth-date-handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> @people-db
             sorted-by-birth-date-asc
             write-value-as-string)})

(defn first-name-handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> @people-db
             sorted-by-first-name-asc
             write-value-as-string)})

(defn last-name-handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> @people-db
             sorted-by-last-name-dsc
             write-value-as-string)})

(defn delete-record-handler
  [_]
  (when-let [records-count (count @people-db)]
    (log/info (format "Delete %s records from the system" records-count))
    (reset! people-db [])
    {:status 200
     :body (write-value-as-string {:deleted-count records-count})
     :headers {"Content-Type" "text/json"}}))

(defn create-record-handler
  [{{{:keys [last-name
             first-name
             gender
             fav-color
             date-of-birth] :as args}
     :body} :parameters}]
  (let [raw-csv-data (str/join "," [last-name
                                    first-name
                                    gender
                                    fav-color
                                    date-of-birth])
        {:keys [last-name
                first-name
                gender
                fav-color
                date-of-birth
                err]} (parse-and-validate
                       {:type :csv
                        :data raw-csv-data})]
    (if-not err
      (do
        (log/info "Adding new record to the system : " args)
        (add-person args)
        {:status 201
         :headers {"Content-Type" "text/json"}
         :body (write-value-as-string {:first-name first-name
                                       :last-name last-name
                                       :gender gender
                                       :fav-color fav-color
                                       :date-of-birth date-of-birth})})
      ;; See: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
      {:status 400 ;; consider this as a bad request for now
       :headers {"Content-Type" "text/json"}
       :body (write-value-as-string {:data args
                                     :err err})})))

;; Save the content of the upload file in the $project-root/uploads
(def resource-path (format "%s%s%s"
                           (System/getProperty "user.dir")
                           File/separator
                           "uploads"))

(defn ^:private file-path
  [path & [filename]]
  (URLDecoder/decode
   (str path File/separator filename)
   "utf-8"))

(defn ^:private upload-file
  "Uploads a file to the target folder"
  [path {:keys [tempfile filename]}]
  (with-open [in (io/input-stream tempfile)
              out (io/output-stream (file-path path filename))]
    (io/copy in out)))

(def app
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get
       {:no-doc true
        :swagger {:info {:title "Simple REST API"
                         :description "Build with reitit-ring"}}
        :handler (swagger/create-swagger-handler)}}]
     ["/records"
      {:swagger {:tags ["records"]}}
      ["/"
       {:get
        {:summary "List all records in the system"
         :handler all-data-handler}

        :post
        {:summary "Add a new record to the system"
         :parameters {:body {:last-name string?,
                             :first-name string?
                             :gender string?
                             :fav-color string?
                             :date-of-birth string?}}
         :responses {200 {:body string?}}
         :handler create-record-handler}

        :delete
        {:summary "Delete all data from the system! (intended for use during test)"
         :handler delete-record-handler}}]

      ["/upload"
       {:post {:summary "Populate database from input file (must have extension .csv, .space, .piped)"
               :parameters {:multipart {:file multipart/temp-file-part}}
               :responses {200 {:body {:name string?, :size int?}}}
               :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                          (let [{:keys [filename size] :as opts} file
                                input-file (format "%s%s%s" resource-path File/separator filename)]
                            (if (file-type input-file)
                              (do
                                (upload-file resource-path opts)
                                (log/info (format "File saved to : %s" input-file))
                                (load-data! input-file)
                                {:status 200
                                 :body {:name filename
                                        :size size}})
                              ;; Not support file type
                              {:status 400 ;; consider this as a bad request for now
                               :headers {"Content-Type" "text/json"}
                               :body (write-value-as-string {:err (format "File not supported, please use .csv, .piped, or .space for extension")})})))}}]
      ["/gender"
       {:get
        {:summary "List all records sorted by gender (female before male) and last-name"
         :handler gender-handler}}]
      ["/lastname"
       {:get
        {:summary "List all records sorted by last-name"
         :handler last-name-handler}}]
      ["/firstname"
       {:get
        {:summary "List all records sorted by first-name"
         :handler first-name-handler}}]
      ["/birthdate"
       {:get
        {:summary "List all records sorted by date-of-birth"
         :handler birth-date-handler}}]]]
    {:exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; swagger feature
                         swagger/swagger-feature

                         ;; query-params & form-params
                         parameters/parameters-middleware

                         ;; content-negotiation
                         muuntaja/format-negotiate-middleware

                         ;; encoding response body
                         muuntaja/format-response-middleware

                         ;; exception handling
                         exception/exception-middleware

                         ;; decoding request body
                         muuntaja/format-request-middleware

                         ;; coercing response bodys
                         coercion/coerce-response-middleware

                         ;; coercing request parameters
                         coercion/coerce-request-middleware

                         ;; multipart
                         multipart/multipart-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-default-handler))))

(defn start []
  (jetty/run-jetty #'app {:port 3000,
                          :join? false})
  (log/info "server running on port 3000"))

(defn -main
  [& args]
  (log/info "Starting main application")
  (start))

(comment
  (start))
