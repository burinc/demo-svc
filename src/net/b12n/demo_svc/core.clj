(ns net.b12n.demo-svc.core
  (:require
   [cljc.java-time.local-date
    :refer [parse]]
   [clojure.spec.alpha
    :as s]
   [clojure.string
    :as str])
  (:import
   [java.time.format
    DateTimeFormatter]))

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

(defn parse-date
  ([date-string]
   (parse-date date-string "MM/dd/yyyy"))
  ([date-string date-format]
   (-> date-string
       (parse (DateTimeFormatter/ofPattern date-format)))))

(defn ^:private valid-date?
  [date-string]
  (try
    (parse-date date-string)
    (catch Exception _e
      false)))

(s/def ::date-of-birth (s/and string? valid-date?))

(s/def ::piped "|")

(s/def ::csv   ",")

(s/def ::space " ")

(s/def ::delimiters #{::piped ::csv ::space})

(s/def ::sort-keys #{:gender-lastname :date-of-birth :first-name :last-name :fav-color})

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
