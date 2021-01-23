(ns net.b12n.demo-svc.server-integration-test
  (:require
   [clj-http.client
    :as http]
   [clojure.java.io
    :as io]
   [clojure.test
    :refer [are deftest is testing]]
   [jsonista.core
    :as json]
   [net.b12n.demo-svc.server
    :refer [app]]))

(deftest ^:integration test-live-system
  (testing "DELETE - /records/delete - clear old data if any"
    (let [{:keys [status body]}
          (-> (http/delete "http://0.0.0.0:3000/records/"
                           {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (-> response :deleted-count int?))))

  (testing "GET - /records - before loading data"
    (let [{:keys [status body]}
          (-> (http/get "http://0.0.0.0:3000/records/"
                        {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (-> response count zero?))))

  (testing "POST - /records - upload data file to the system"
    (let [file (clojure.java.io/file "resources/data.csv")
          multipart-temp-file-part {:tempfile file
                                    :size (.length file)
                                    :filename (.getName file)
                                    :content-type "application/json;"}]
      (is (= (-> {:request-method :post
                  :uri "/records/upload"
                  :multipart-params {:file multipart-temp-file-part}}
                 app
                 :body
                 slurp
                 (json/read-value json/keyword-keys-object-mapper))
             {:name "data.csv"
              :size 118}))))

  (testing "POST - /records - valid entry"
    (let [payload {:first-name "Robby"
                   :last-name "Jackson"
                   :gender "M"
                   :fav-color "Red"
                   :date-of-birth "10/20/1980"}
          {:keys [body status]}
          (http/post "http://0.0.0.0:3000/records/"
                     {:accept :json
                      :body (json/write-value-as-string payload)
                      :content-type :json})]
      (is (= 201 status))
      (is (= payload (-> body (json/read-value json/keyword-keys-object-mapper))))))

  (testing "POST - /records - invalid entry"
    (let [payload {:first-name "Robby"
                   :last-name "Jackson"
                   :gender "X" ;; Note: invalid gender
                   :fav-color "Red"
                   :date-of-birth "10/20/1980"}]
      (try
        (http/post "http://0.0.0.0:3000/records/"
                   {:accept :json
                    :body (json/write-value-as-string payload)
                    :content-type :json})
        (catch Exception e
          (is (= (-> (ex-data e) :body (json/read-value json/keyword-keys-object-mapper) :err)
                 "\"X\" - failed: valid-gender? in: [2] at: [:gender] spec: :net.b12n.demo-svc.core/gender\n"))))))

  (testing "GET - /records/gender"
    (let [{:keys [status body]}
          (-> (http/get "http://0.0.0.0:3000/records/gender"
                        {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (> (count response) 0))))

  (testing "GET - /records/lastname"
    (let [{:keys [status body]}
          (-> (http/get "http://0.0.0.0:3000/records/lastname"
                        {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (> (count response) 0))))

  (testing "GET - /records/firstname"
    (let [{:keys [status body]}
          (-> (http/get "http://0.0.0.0:3000/records/firstname"
                        {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (> (count response) 0))))

  (testing "GET - /records/birthdate"
    (let [{:keys [status body]}
          (-> (http/get "http://0.0.0.0:3000/records/birthdate"
                        {:accept :json}))
          response (-> body (json/read-value json/keyword-keys-object-mapper))]
      (are [expected result] (= expected result)
        200  status
        true (> (count response) 0)))))
