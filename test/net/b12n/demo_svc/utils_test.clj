(ns net.b12n.demo-svc.utils-test
  (:require
   [clojure.test
    :refer
    [are
     deftest
     is
     testing]]
   [net.b12n.demo-svc.utils
    :refer
    [file-type
     map-keys
     parse-date
     sorted-by-birth-date-asc
     sorted-by-first-name-asc
     sorted-by-gender-then-last-name-asc
     sorted-by-last-name-dsc
     transform-keys]])
  (:import
   [java.time
    DateTimeException
    LocalDate]))

(deftest mapkeys-test
  (testing "mapkeys"
    (is (= (map-keys name {:a 1 :b 2}) {"a" 1, "b" 2}))))

(deftest transform-keys-test
  (testing "transform-keys"
    (is (= (transform-keys {"--profile" "dev", "--region" "us-east-1"})
           {:profile "dev", :region "us-east-1"}))
    (is (= (transform-keys {"--profile" "dev", "create" "true"})
           {:profile "dev", :create "true"}))))

(deftest parse-date-test
  (testing "parse-date"
    (is (thrown? DateTimeException (parse-date "24/01/2014" "MM/dd/yyyy")))
    (is (= (type (parse-date "12/01/2014" "MM/dd/yyyy")) LocalDate))))

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

(deftest file-type-test
  (testing "basic file type"
    (are [result arg-map] (= result (file-type (:filename arg-map)))
      nil    {:filename "test.txt"}
      :csv   {:filename "data.csv"}
      :space {:filename "data.space"}
      :piped {:filename "data.piped"}
      :piped {:filename "data.PipEd"})))
