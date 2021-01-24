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
            file-type]]
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
