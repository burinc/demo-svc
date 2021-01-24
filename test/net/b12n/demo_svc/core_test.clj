(ns net.b12n.demo-svc.core-test
  (:require
   [clojure.string
    :as str]
   [clojure.test
    :refer [deftest is testing]]
   [net.b12n.demo-svc.core
    :refer [parse-and-validate
            parse-date
            parse-record]])
  (:import
   [java.time
    DateTimeException
    LocalDate]))

#_
(deftest parse-date-test
  (testing "parse-date"
    (is (thrown? DateTimeException (parse-date "24/01/2014" "MM/dd/yyyy")))
    (is (= (type (parse-date "12/01/2014" "MM/dd/yyyy"))) LocalDate)))

(deftest parse-record-test
  (testing "parse-record"
    (let [csv-input "Smith,Jack,M,Blue,06/18/1980"
          csv-data {:type :csv
                    :data csv-input}
          piped-data {:type :piped
                      :data (str/replace csv-input "," "|")}
          space-data {:type :space
                      :data (str/replace csv-input "," " ")}]
      (is (= (parse-record csv-data)
             (parse-record piped-data)
             (parse-record space-data)
             ["Smith" "Jack" "M" "Blue" "06/18/1980"])))))

(deftest parse-and-validate-test
  (testing "parse-and-validate : valid data"
    (let [csv-input "Smith,Jack,M,Blue,06/18/1980"
          csv-data {:type :csv
                    :data csv-input}
          piped-data {:type :piped
                      :data (str/replace csv-input "," "|")}
          space-data {:type :space
                      :data (str/replace csv-input "," " ")}
          expected {:last-name "Smith",
                    :first-name "Jack",
                    :gender "M",
                    :fav-color "Blue",
                    :date-of-birth "06/18/1980",
                    :err nil}]
      (is (= expected (parse-and-validate csv-data)))
      (is (= expected (parse-and-validate piped-data)))
      (is (= expected (parse-and-validate space-data))))))

(testing "parse-and-validate : invalid date"
  (let [csv-input "Smith,Jack,M,Blue,06/18/19XX"
        csv-data {:type :csv
                  :data csv-input}
        piped-data {:type :piped
                    :data (str/replace csv-input "," "|")}
        space-data {:type :space
                    :data (str/replace csv-input "," " ")}]
    (is (= (parse-and-validate csv-data)
           (parse-and-validate piped-data)
           (parse-and-validate space-data)
           {:err "\"06/18/19XX\" - failed: valid-date? in: [4] at: [:date-of-birth] spec: :net.b12n.demo-svc.core/date-of-birth\n"}))))

(testing "parse-and-validate : invalid gender"
  (let [csv-input "Smith,Jack,X,Blue,06/18/1989"
        csv-data {:type :csv
                  :data csv-input}
        piped-data {:type :piped
                    :data (str/replace csv-input "," "|")}
        space-data {:type :space
                    :data (str/replace csv-input "," " ")}]
    (is (= (parse-and-validate csv-data)
           (parse-and-validate piped-data)
           (parse-and-validate space-data)
           {:err "\"X\" - failed: valid-gender? in: [2] at: [:gender] spec: :net.b12n.demo-svc.core/gender\n"}))))
