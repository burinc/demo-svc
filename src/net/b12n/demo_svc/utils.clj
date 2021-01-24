(ns net.b12n.demo-svc.utils
  (:require
   [clojure.string
    :as str]
   [cljc.java-time.local-date
    :refer [parse]])
  (:import
   [java.time.format
    DateTimeFormatter]))

(defn map-keys
  "Given a function and a map, returns the map resulting from applying
  the function to each key.
  e.g. (map-keys name {:a 1 :b 2 :c 3}) ;;=> {\"a\" 1, \"b\" 2, \"c\" 3}
  "
  [f m]
  (zipmap (map f (keys m)) (vals m)))

(defn transform-keys
  "Transform the options arguments

  ;; a) where keys contain `--`
  (transform-keys {\"--profile\" \"dev\", \"--region\" \"us-east-one\" })
  ;;=> {:profile \"dev\" :region \"us-east-1\"}

  ;; b) work with keys that does not have `--` in it
  (transform-keys {\"--profile\" \"dev\", \"create\" \"true\" })
  ;;=> {:profile \"dev\" :create \"true\"} "
  [opts]
  (map-keys (fn [x] (-> x (str/replace-first "--" "") keyword)) opts))

(defn parse-date
  ([date-string]
   (parse-date date-string "MM/dd/yyyy"))
  ([date-string date-format]
   (-> date-string
       (parse (DateTimeFormatter/ofPattern date-format)))))

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
