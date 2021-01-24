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
            sorted-by-first-name-asc
            sorted-by-last-name-dsc
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
  [lines file-type]
  (->> (process-lines lines :csv)
       (filter (fn [x]
                 (-> x :err not)))
       (map (fn [x] (dissoc x :err)))))

#_(comment
    ;; filter out the valid input only
    (when-let [lines (-> "./resources/data-with-invalid-lines.csv"
                         slurp
                         str/split-lines)]
      (filter-valid-lines lines :csv)))

(defn filter-invalid-lines
  [lines file-type]
  (->> (process-lines lines :csv)
       (filter (fn [x]
                 (-> x :err)))))

#_(comment
    ;; filter out the invalid input only
    (when-let [lines (-> "./resources/data-with-invalid-lines.csv"
                         slurp
                         str/split-lines)]
      (filter-invalid-lines lines :csv)))

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
        (let [invalid-lines (filter-invalid-lines raw-lines file-type)
              _ (if-not (-> invalid-lines count zero?)
                  (doseq [line invalid-lines]
                    (log/warn line)))
              valid-lines (filter-valid-lines raw-lines file-type)]

          (println "a) sorted by gender and then last name (ascending)")
          (-> valid-lines sorted-by-gender-then-last-name-asc pprint)

          (println "b) sorted by last name (descending)")
          (-> valid-lines sorted-by-last-name-dsc pprint)

          (println "c) sorted by first name (ascending)")
          (-> valid-lines sorted-by-last-name-dsc pprint)

          (println "d) sorted by date of birth (ascending)")
          (-> valid-lines sorted-by-birth-date-asc pprint)))
      (do
        (log/warn (format "Must be one of the following format %s."
                          (->> supported-formats
                               (map name)
                               (str/join ", "))))))))

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
