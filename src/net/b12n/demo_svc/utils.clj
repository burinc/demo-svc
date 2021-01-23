(ns net.b12n.demo-svc.utils
  (:require
   [clojure.string :as str]))

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
