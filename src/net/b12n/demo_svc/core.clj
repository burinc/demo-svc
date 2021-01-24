(ns net.b12n.demo-svc.core
  (:require
   [cljc.java-time.local-date
    :refer [parse]]
   [clojure.spec.alpha
    :as s]
   [clojure.string
    :as str]
   [clojure.tools.logging :as log]
   [docopt.core :as docopt]
   [me.raynes.fs
    :refer [expand-home]]
   [net.b12n.demo-svc.utils
    :refer [transform-keys]])
  (:import
   [java.time.format
    DateTimeFormatter])
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

(defn ^:private load-and-display
  [{:keys [input-file file-type]}]
  (let [file-type (keyword file-type)
        input-file (expand-home input-file)]
    (log/info (format "Load data from %s of type %s"
                      input-file
                      file-type))
    ;; Now we can check and confirm if the file is valid input
    (if (s/valid? ::supported-formats file-type)
      (when-let [lines (-> input-file
                           slurp
                           (str/split-lines))]
        (let [valid-lines (->> (for [line lines
                                     :let [{:keys [last-name
                                                   first-name
                                                   gender
                                                   fav-color
                                                   date-of-birth
                                                   err] :as args} (parse-and-validate {:type file-type
                                                                                       :data line})]]
                                 ;; NOTE: will just log the error and take only a valid line
                                 (if err (log/warn (format "Invalid line of data : %s" {:line line
                                                                                        :err err}))
                                     ;; Return the data without the error
                                     (dissoc args :err)))
                               ;; Remove the rows that contain the error e.g. nil in the result
                               (filter identity))]
          (println valid-lines)))
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
