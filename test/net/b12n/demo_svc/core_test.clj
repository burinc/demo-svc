(ns net.b12n.demo-svc.core-test
  (:require
   [clojure.string
    :as str]
   [clojure.test
    :refer [deftest is testing]]
   [net.b12n.demo-svc.core
    :refer [filter-invalid-lines
            filter-valid-lines
            parse-and-validate
            parse-record]])
  (:import
   [java.time
    DateTimeException
    LocalDate]))

(deftest filter-valid-lines-test
  (testing "filter-valid-lines"
    (let [lines (-> "./resources/data-with-invalid-lines.csv"
                    slurp
                    str/split-lines)]
      (is (= (filter-valid-lines lines :csv)
             '({:last-name "Henry",   :first-name "Jill", :gender "F", :fav-color "White", :date-of-birth "10/18/1980"}
               {:last-name "Johnson", :first-name "Josh", :gender "M", :fav-color "Blue",  :date-of-birth "06/18/1990"}
               {:last-name "Barry",   :first-name "Jane", :gender "F", :fav-color "Pink",  :date-of-birth "07/18/1950"}
               {:last-name "Smith",   :first-name "John", :gender "M", :fav-color "Red",   :date-of-birth "06/18/2000"}))))))

(deftest filter-invalid-lines-test
  (testing "filter-invalid-lines"
    (let [lines (-> "./resources/data-with-invalid-lines.csv"
                    slurp
                    str/split-lines)]
      (is (= (filter-invalid-lines lines :csv)
             '({:data "Johnson,Josh,M,Blue,06/18/19XX",
                :file-type :csv,
                :err "\"06/18/19XX\" - failed: valid-date? in: [4] at: [:date-of-birth] spec: :net.b12n.demo-svc.core/date-of-birth\n"}
               {:data "Barry,Jane,X,Pink,07/18/1950",
                :file-type :csv,
                :err "\"X\" - failed: valid-gender? in: [2] at: [:gender] spec: :net.b12n.demo-svc.core/gender\n"}))))))

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
