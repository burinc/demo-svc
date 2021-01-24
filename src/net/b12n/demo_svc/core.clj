(ns net.b12n.demo-svc.core
  (:require
   [clojure.spec.alpha
    :as s]
   [clojure.string
    :as str]
   [clojure.pprint
    :refer [pprint]]
   [clojure.tools.logging
    :as log]
   [docopt.core :as docopt]
   [me.raynes.fs
    :refer [expand-home]]
   [net.b12n.demo-svc.utils
    :refer [parse-date
            transform-keys
            sorted-by-gender-then-last-name-asc
            sorted-by-last-name-dsc
            sorted-by-first-name-asc
            sorted-by-birth-date-asc]])
  (:gen-class))

;; specs
(s/def ::last-name string?)

(s/def ::first-name string?)

(def ^:const valid-gender #{"male" "female" "m" "f"})

(defn ^:private valid-gender?
  [s]
  (and s (->> s
              str/lower-case
              (get valid-gender))))

(s/def ::gender (s/and string?
                       valid-gender?))

(s/def ::fav-color string?)

(defn ^:private valid-date?
  [date-string]
  (try
    (parse-date date-string)
    (catch Exception _e
      false)))

(def supported-formats #{:piped :csv :space})

(s/def ::date-of-birth (s/and string? valid-date?))

(s/def ::piped "|")

(s/def ::csv   ",")

(s/def ::space " ")

(s/def ::delimiters #{::piped ::csv ::space})

(s/def ::supported-formats supported-formats)

(s/def ::input-line (s/cat :last-name     ::last-name
                           :first-name    ::first-name
                           :gender        ::gender
                           :fav-color     ::fav-color
                           :date-of-birth ::date-of-birth))

(defmulti parse-record :type)

(defmethod parse-record
  :piped
  [{:keys [data]}]
  (str/split data #"\|"))

(defmethod parse-record
  :csv
  [{:keys [data]}]
  (str/split data #","))

(defmethod parse-record
  :space
  [{:keys [data]}]
  (str/split data #"\s+"))

(defn parse-and-validate
  [{:keys [type data] :as input}]
  (let [result (parse-record input)]
    (if (s/valid? ::input-line result)
      (assoc (s/conform ::input-line result)
             :err nil)
      {:err (s/explain-str ::input-line result)})))

(def usage "srk - Simple Record Keeper library

Usage:
  srk [--input-file=<input-file> --file-type=<file-type> | --help ]

Options:
  -i, --input-file=<input-file>  Input file to use [default: data.csv]
  -t, --file-type=<file-type>    File type (one of csv, piped, and space) [default: csv]
  -h, --help                     Print this usage

  Example Usage:
  # a) Load input file of type 'csv' to the system
  srk -i ./resources/data.csv -t csv

  # b) Load input file of type 'piped' to the system
  srk -i ./resources/data.piped -t piped

  # c) Load input file of type 'space' to the system
  srk -i ./resoures/data.space -t space

  # c) Show help
  srk -h")

(defn process-lines
  [lines file-type]
  (for [line lines
        :let [{:keys [last-name first-name gender fav-color date-of-birth err] :as args}
              (parse-and-validate {:type file-type :data line})]]
    (if err
      {:data      line
       :file-type file-type
       :err       err}
      {:data      (dissoc args :err)
       :file-type file-type
       :err       nil})))

(defn filter-valid-lines
  "Return the collection of valid maps

  '({:last-name     \"Smith\"
     :first-name    \"John\"
     :gender        \"M\"
     :fav-color     \"Blue\"
     :date-of-birth \"...\"},
     ; ...
     )"
  [lines file-type]
  (->> (process-lines lines :csv)
       (filter (fn [x]
                 (-> x :err not)))
       (map :data)))

(defn filter-invalid-lines
  "Return the collection of lines that have errors.
  '({:data \"...\" :err \"error details 1\"}
     ;; ...
    {:data \"...\" :err \"error details n\"})"
  [lines file-type]
  (->> (process-lines lines :csv)
       (filter (fn [x]
                 (-> x :err)))))

(defn ^:private display-records
  [records]
  (println "a) sorted by gender and then last name (ascending)\n")
  (-> records sorted-by-gender-then-last-name-asc pprint)

  (println "b) sorted by last name (descending)\n")
  (-> records sorted-by-last-name-dsc pprint)

  (println "c) sorted by first name (ascending)\n")
  (-> records sorted-by-first-name-asc pprint)

  (println "d) sorted by date of birth (ascending)\n")
  (-> records sorted-by-birth-date-asc pprint))

(defn ^:private load-and-display
  [{:keys [input-file file-type]}]
  (let [file-type (keyword file-type)
        input-file (expand-home input-file)]
    (log/info (format "Load data from %s of type %s"
                      input-file
                      file-type))
    ;; Now we can check and confirm if the file is valid input
    (if (s/valid? ::supported-formats file-type)
      (when-let [raw-lines (-> input-file
                               slurp
                               (str/split-lines))]
        (filter-valid-lines raw-lines file-type)
        (let [invalid-lines (filter-invalid-lines raw-lines file-type)
              _ (when-not (-> invalid-lines count zero?)
                  (doseq [{:keys [data err]} (filter-invalid-lines raw-lines :csv)]
                    (log/warn (format "Invalid line : `%s` due to `%s`" data err))))
              valid-lines (filter-valid-lines raw-lines file-type)]
          (display-records valid-lines)))
      (log/warn (format "Must be one of the following format %s."
                        (->> supported-formats (map name) (str/join ", ")))))))

#_(load-and-display {:input-file "./resources/data-with-invalid-lines.csv" :file-type :csv})

(defn ^:private run
  [& [{:keys [input-file file-type help] :as args}]]
  (cond
    help
    (println usage)

    :else
    (-> args
        (select-keys [:input-file :file-type])
        load-and-display)))

(defn -main
  [& args]
  (docopt/docopt usage
                 args
                 (fn [arg-map]
                   (-> arg-map
                       transform-keys
                       run)))
  (System/exit 0))
