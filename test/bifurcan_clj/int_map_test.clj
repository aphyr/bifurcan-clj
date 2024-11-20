(ns bifurcan-clj.int-map-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [bifurcan-clj [core :as b]
                                    [int-map :as i]
                                    [map :as m]
                                    [set :as s]]))

(deftest int-map-test
  (testing "empty"
    (is (= 0 (b/size (i/int-map))))))

(deftest from-test
  (testing "nil"
    (is (= (i/int-map) (i/from nil))))
  (testing "clojure map"
    (is (= {1 :x, 2 :y} (datafy (i/from {1 :x, 2 :y})))))
  (testing "vector of pairs"
    (is (= {1 :x, 2 :y} (datafy (i/from [[1 :x] [2 :y]])))))
  (testing "lazy seq of pairs"
    (is (= {1 :x, 2 :y} (datafy (i/from (map vector [1 2] [:x :y]))))))
  (testing "map"
    (is (= {1 :x, 2 :y} (datafy (i/from (m/from {1 :x, 2 :y})))))))

(deftest get-test
  (let [m (i/from {1 :x})]
    (is (= :x         (i/get m 1)))
    (is (= nil        (i/get m 2)))
    (is (= :x         (i/get m 1 :not-found)))
    (is (= :not-found (i/get m 3 :not-found)))))

(deftest get-or-create-test
  (let [m (i/from {1 :x})]
    (is (= :default (m/get-or-create m 2 (fn [] :default))))))

(deftest contains?-test
  (is (i/contains? (i/from {1 :x}) 1)))

(deftest entries-test
  (is (= [[1 :x]] (datafy (m/entries (i/from {1 :x}))))))

(let [m (i/from {1 :x, 3 :y})]
  (deftest index-of-test
    (is (= nil (i/index-of m 2)))
    (is (= 0   (i/index-of m 1))))

  (deftest floor-index-test
    (is (= nil (i/floor-index m 0)))
    (is (= 0   (i/floor-index m 1)))
    (is (= 0   (i/floor-index m 2)))
    (is (= 1   (i/floor-index m 3)))
    (is (= 1   (i/floor-index m 4))))

  ;(deftest inclusive-floor-index-test
  ;  (is (= nil (i/inclusive-floor-index m 0)))
  ;  (is (= 0   (i/inclusive-floor-index m 1)))
  ;  (is (= 0   (i/inclusive-floor-index m 2)))
  ;  (is (= 1   (i/inclusive-floor-index m 3)))
  ;  (is (= 1   (i/inclusive-floor-index m 4))))

  (deftest ceil-index-test
    (is (= 0   (i/ceil-index m 0)))
    (is (= 0   (i/ceil-index m 1)))
    (is (= 1   (i/ceil-index m 2)))
    (is (= 1   (i/ceil-index m 3)))
    (is (= nil (i/ceil-index m 4)))))

(deftest keys-test
  (is (= #{1 2} (datafy (m/keys (i/from {1 :x, 2 :y}))))))

(deftest values-test
  (is (= [:x :y] (datafy (m/values (i/from {1 :x, 2 :y}))))))

(deftest ^:buggy map-values-test
  (let [m (i/from {1 10, 3 30})]
    (testing "IMap"
      ; This fails due to a bug I think
      (is (= {1 11 3 31} (datafy (m/map-values m (fn [k v]
                                                   (prn :k k :v v)
                                                   (inc v)))))))))

(deftest contains-all?-test
  (testing "map"
    (is (= true (m/contains-all? (i/from {1 :x, 2 :y})
                                 (m/from {1 :z})))))
  (testing "set"
    (is (= true (m/contains-all? (i/from {1 :x, 2 :y})
                                 (s/from #{1}))))))

(deftest contains-any?-test
  (testing "map"
    (is (= true (m/contains-any? (i/from {1 :x, 2 :y})
                                 (m/from {1 :z, 3 :q})))))
  (testing "set"
    (is (= true (m/contains-any? (i/from {1 :x, 2 :y})
                                 (s/from #{1 3}))))))

(deftest merge-test
  (testing "lww"
    (is (= {1 :x 2 :y 3 :z}
           (datafy (m/merge (i/from {1 :x 2 :x})
                            (i/from {2 :y 3 :z}))))))

  (testing "merge-fn"
    (is (= {1 1, 2 3, 3 3}
           (datafy (m/merge (i/from {1 1, 2 1})
                            (i/from {2 2, 3 3})
                            +))))))

(deftest difference-test
  (testing "map"
    (is (= {1 1}
           (datafy (m/difference (i/from {1 1 2 1})
                                 (i/from {2 2 3 2}))))))
  (testing "set"
    (is (= {1 1}
           (datafy (m/difference (i/from {1 1 2 1})
                                 (s/from #{2 3})))))))

(deftest difference-test
  (is (= {1 1 2 2 3 2}
         (datafy (m/union (i/from {1 1 2 1})
                          (i/from {2 2 3 2}))))))

(deftest intersection-test
  (testing "map"
    (is (= {2 1}
           (datafy (m/intersection (i/from {1 1 2 1})
                                   (i/from {2 2 3 2}))))))
  (testing "set"
    (is (= {2 1}
           (datafy (m/intersection (i/from {1 1 2 1})
                                    (s/from #{2 3})))))))

(deftest put-test
  (is (= {1 2} (-> (m/map)
                    (m/put 1 1)
                    (m/put 1 2)
                    datafy))))

(deftest update-test
  (is (= {1 2} (-> (i/from {1 1}) (i/update 1 inc) datafy))))

(deftest remove-test
  (is (= {1 1} (-> {1 1 2 2} i/from (i/remove 2) datafy))))

(deftest slice-test
  (let [m (i/from {1 :a, 2 :b, 3 :c, 4 :d})]
    (testing "empty"
      (is (= {} (datafy (i/slice m 0 0)))))
    (testing "single"
      (is (= {1 :a} (datafy (i/slice m 1 1))))
      (is (= {4 :d} (datafy (i/slice m 4 4)))))
    (testing "middle"
      (is (= {2 :b, 3 :c} (datafy (i/slice m 2 3)))))
    (testing "over the edge"
      (is (= {1 :a, 2 :b} (datafy (i/slice m 0 2))))
      (is (= {3 :c, 4 :d} (datafy (i/slice m 3 8)))))))
