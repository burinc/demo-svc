(ns net.b12n.demo-svc.server-test
  (:require
   [clojure.java.io
    :as io]
   [clojure.string
    :as str]
   [clojure.test
    :refer [are
            deftest
            is
            testing]]
   [jsonista.core
    :refer [read-value
            write-value-as-string
            keyword-keys-object-mapper]]
   [net.b12n.demo-svc.server
    :refer [app
            file-type
            sorted-by-birth-date-asc
            sorted-by-gender-then-last-name-asc
            sorted-by-last-name-dsc
            sorted-by-first-name-asc]]
   [ring.mock.request
    :refer [request]]))

(deftest file-type-test
  (testing "basic file type"
    (are [result arg-map] (= result (file-type (:filename arg-map)))
      nil    {:filename "test.txt"}
      :csv   {:filename "data.csv"}
      :space {:filename "data.space"}
      :piped {:filename "data.piped"}
      :piped {:filename "data.PipEd"})))

(def jill-henry
  {:last-name "Henry",
   :first-name "Jill",
   :gender "F",
   :fav-color "White",
   :date-of-birth "10/18/1980"})

(def josh-johnson
  {:last-name "Johnson",
   :first-name "Josh",
   :gender "M",
   :fav-color "Blue",
   :date-of-birth "06/18/1990"})

(def jane-barry
  {:last-name "Barry",
   :first-name "Jane",
   :gender "F",
   :fav-color "Pink",
   :date-of-birth "07/18/1950"})

(def john-smith
  {:last-name "Smith",
   :first-name "John",
   :gender "M",
   :fav-color "Red",
   :date-of-birth "06/18/2000"})

(def adam-smith
  {:last-name "Smith",
   :first-name "Adam",
   :gender "M",
   :fav-color "Purple",
   :date-of-birth "12/20/1975"})

(def ceri-zappa
  {:last-name "Zappa",
   :first-name "Ceri",
   :gender "F",
   :fav-color "Green",
   :date-of-birth "04/26/1957"})

(def data
  [jill-henry
   josh-johnson
   jane-barry
   john-smith
   adam-smith
   ceri-zappa])

(deftest sort-functions
  (testing "sorted-by-birth-date-ascending"
    (is (= (->> data
                sorted-by-birth-date-asc
                (map (juxt :date-of-birth :gender :last-name :first-name :fav-color)))
           '(["07/18/1950" "F" "Barry" "Jane" "Pink"]
             ["04/26/1957" "F" "Zappa" "Ceri" "Green"]
             ["12/20/1975" "M" "Smith" "Adam" "Purple"]
             ["10/18/1980" "F" "Henry" "Jill" "White"]
             ["06/18/1990" "M" "Johnson" "Josh" "Blue"]
             ["06/18/2000" "M" "Smith" "John" "Red"]))))

  (testing "sorted-by-first-name-ascending"
    (is (= (->> data
                sorted-by-first-name-asc
                (map (juxt :first-name :last-name :gender :first-name :fav-color :date-of-birth)))
           '(["Adam" "Smith" "M" "Adam" "Purple" "12/20/1975"]
             ["Ceri" "Zappa" "F" "Ceri" "Green" "04/26/1957"]
             ["Jane" "Barry" "F" "Jane" "Pink" "07/18/1950"]
             ["Jill" "Henry" "F" "Jill" "White" "10/18/1980"]
             ["John" "Smith" "M" "John" "Red" "06/18/2000"]
             ["Josh" "Johnson" "M" "Josh" "Blue" "06/18/1990"]))))

  (testing "sorted-by-gender-then-last-name-ascending"
    (is (= (->> data
                sorted-by-gender-then-last-name-asc
                (map (juxt :gender :first-name :last-name :fav-color :date-of-birth)))
           '(["F" "Jane" "Barry" "Pink" "07/18/1950"]
             ["F" "Jill" "Henry" "White" "10/18/1980"]
             ["F" "Ceri" "Zappa" "Green" "04/26/1957"]
             ["M" "Josh" "Johnson" "Blue" "06/18/1990"]
             ["M" "John" "Smith" "Red" "06/18/2000"]
             ["M" "Adam" "Smith" "Purple" "12/20/1975"]))))

  (testing "sorted-by-last-name-descending"
    (is (= (->> data
                sorted-by-last-name-dsc
                (map (juxt :last-name :first-name :gender :fav-color :date-of-birth)))
           '(["Zappa" "Ceri" "F" "Green" "04/26/1957"]
             ["Smith" "John" "M" "Red" "06/18/2000"]
             ["Smith" "Adam" "M" "Purple" "12/20/1975"]
             ["Johnson" "Josh" "M" "Blue" "06/18/1990"]
             ["Henry" "Jill" "F" "White" "10/18/1980"]
             ["Barry" "Jane" "F" "Pink" "07/18/1950"])))))

(deftest basic-routing
  (testing "GET"
    (are [result arg-map] (= result (-> (request :get (:uri arg-map)) app :status))
      200 {:uri "/records/gender"}
      200 {:uri "/records/birthdate"}
      200 {:uri "/records/lastname"}
      200 {:uri "/records/firstname"})

    (is (= '("/records/birthdate"
             "/records/firstname"
             "/records/gender"
             "/records/"
             "/records/upload"
             "/records/lastname")
           (as-> (request :get "/swagger.json") $
             (app $)
             (:body $)
             (slurp $)
             (read-value $ keyword-keys-object-mapper)
             (:paths $)
             (keys $)
             (map str $)
             (map (fn [x] (str/replace x ":" "")) $)))))

  (testing "Upload unsupported file extension"
    (let [file (io/file "resources/unsupported-data.txt")
          multipart-temp-file-part {:tempfile file
                                    :size (.length file)
                                    :filename (.getName file)
                                    :content-type "application/text;"}]
      (is (= (-> {:request-method :post
                  :uri "/records/upload"
                  :multipart-params {:file multipart-temp-file-part}}
                 app
                 :body)
             (write-value-as-string {:err "File not supported, please use .csv, .piped, or .space for extension"}))))))
