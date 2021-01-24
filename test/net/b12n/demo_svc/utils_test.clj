(ns net.b12n.demo-svc.utils-test
  (:require
   [clojure.test
    :refer [deftest is testing]]
   [net.b12n.demo-svc.utils
    :refer [map-keys transform-keys]]))

(deftest mapkeys-test
  (testing "mapkeys"
    (is (= (map-keys name {:a 1 :b 2}) {"a" 1, "b" 2}))))

(deftest transform-keys-test
  (testing "transform-keys"
    (is (= (transform-keys {"--profile" "dev", "--region" "us-east-1" })
           {:profile "dev", :region "us-east-1"}))
    (is (= (transform-keys {"--profile" "dev", "create" "true" })
           {:profile "dev", :create "true"}))))
