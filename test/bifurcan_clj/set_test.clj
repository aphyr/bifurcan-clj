(ns bifurcan-clj.set-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [bifurcan-clj [core :as b]
                                    [list :as bl]
                                    [map :as bm]
                                    [set :as bs]]))

(deftest empty-test
  (is (not (b/linear? bs/empty)))
  (is (= 0 (b/size bs/empty)))
  (is (= #{} (datafy bs/empty))))

(deftest set-test
  (testing "empty"
    (is (= bs/empty (bs/set))))

  (testing "custom equality/hash"
    (is (= #{:x :y}
           (-> (bs/set #(hash (name %)) (fn [a b] (= (name a) (name b))))
               (bs/add :x)
               (bs/add "x")
               (bs/add :y)
               (bs/add "y")
               datafy)))))

(deftest from-test
  (testing "nil"
    (is (= (bs/set) (bs/from nil))))
  (testing "list"
    (is (= #{1 2} (datafy (bs/from (bl/list 1 2))))))
  (testing "iterator"
    (is (= #{1 2} (datafy (bs/from (.iterator [1 2]))))))
  (testing "vec"
    (is (= #{1 2} (datafy (bs/from [1 2])))))
  (testing "lazy seq"
    (is (= #{1 2} (datafy (bs/from (map identity [1 2])))))))

(deftest contains?-test
  (is (bs/contains? (bs/from #{1 2}) 2)))

(deftest elements-test
  (is (= [1 2 3] (-> #{1 2 3} bs/from bs/elements datafy))))

(deftest zip-test
  (is (= {:x "x" :y "y"} (-> #{:x :y} bs/from (bs/zip name) datafy))))

(deftest index-of-test
  ; Depends on hash order
  (is (= 0 (-> #{:x :y} bs/from (bs/index-of :y))))
  (is (= nil (-> #{:x :y} bs/from (bs/index-of :z)))))

(deftest contains-all?-test
  (testing "set"
    (is (-> #{:x :y} bs/from (bs/contains-all? (bs/from [:x])))))
  (testing "map"
    (is (-> #{:x :y} bs/from (bs/contains-all? (bm/from {:x 1}))))))

(deftest contains-any?-test
  (testing "set"
    (is (-> #{:x :y} bs/from (bs/contains-any? (bs/from [:x :z])))))
  (testing "map"
    (is (-> #{:x :y} bs/from (bs/contains-any? (bm/from {:x 1 :z 2}))))))

(deftest add-test
  (is (= #{:x :y} (-> #{:y} bs/from (bs/add :x) datafy))))

(deftest remove-test
  (is (= #{:x} (-> #{:x :y} bs/from (bs/remove :y) datafy))))

(deftest union-test
  (is (= #{:x :y :z} (datafy (bs/union (bs/from [:x :y]) (bs/from [:y :z]))))))

(deftest difference-test
  (is (= #{:x} (datafy (bs/difference (bs/from [:x :y :z])
                                      (bs/from [:y :z]))))))

(deftest intersection-test
  (is (= #{:y} (datafy (bs/intersection (bs/from [:x :y]) (bs/from [:y :z]))))))
