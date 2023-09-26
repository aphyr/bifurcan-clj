(ns bifurcan-clj.map-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [bifurcan-clj [core :as b]
                                    [map :as bm]
                                    [set :as bs]]))

(deftest empty-test
  (is (not (b/linear? bm/empty)))
  (is (= 0 (b/size bm/empty)))
  (is (= {} (datafy bm/empty))))

(deftest map-test
  (testing "empty"
    (is (= bm/empty (bm/map))))
  (testing "custom equality/hashing"
    (is (= {:x 3}
           (-> (bm/map (fn [x] (hash (name x)))
                       (fn [x y] (= (name x) (name y))))
               (bm/put :x 1)
               (bm/put "x" 2 +)
               datafy)))))

(deftest from-test
  (testing "clojure map"
    (is (= {:x 1, :y 2} (datafy (bm/from {:x 1, :y 2})))))
  (testing "seq of pairs"
    (is (= {:x 1, :y 2} (datafy (bm/from (map identity [[:x 1] [:y 2]]))))))
  (testing "vec of pairs"
    (is (= {:x 1, :y 2} (datafy (bm/from [[:x 1] [:y 2]])))))
  (testing "map"
    (is (= {:x 1, :y 2} (datafy (bm/from (bm/from {:x 1, :y 2})))))))

(deftest get-test
  (let [m (bm/from {:x 1})]
    (is (= 1 (bm/get m :x)))
    (is (= nil (bm/get m :y)))
    (is (= 1 (bm/get m :x :not-found)))
    (is (= :not-found (bm/get m :y :not-found)))))

(deftest get-or-create-test
  (let [m (bm/from {:x 1})]
    (is (= :default (bm/get-or-create m :y (fn [] :default))))))

(deftest contains?-test
  (is (bm/contains? (bm/from {:x 1}) :x)))

(deftest entries-test
  (is (= [[:x 1]] (datafy (bm/entries (bm/from {:x 1}))))))

(deftest index-of-test
  (is (= nil (bm/index-of bm/empty :y)))
  (is (= 0 (bm/index-of (bm/from {:x 1}) :x))))

(deftest keys-test
  (is (= #{:x :y} (datafy (bm/keys (bm/from {:x 1 :y 2}))))))

(deftest values-test
  (is (= [1 2] (sort (datafy (bm/values (bm/from {:x 1 :y 2})))))))

(deftest map-values-test
  (is (= {:x 2 :y 3} (datafy (bm/map-values (bm/from {:x 1 :y 2})
                                            (fn [k v] (inc v)))))))

(deftest contains-all?-test
  (testing "map"
    (is (= true (bm/contains-all? (bm/from {:x 1 :y 2})
                                  (bm/from {:x 3})))))
  (testing "set"
    (is (= true (bm/contains-all? (bm/from {:x 1 :y 2})
                                  (bs/from #{:x}))))))

(deftest contains-any?-test
  (testing "map"
    (is (= true (bm/contains-any? (bm/from {:x 1 :y 2})
                                  (bm/from {:x 3 :z 4})))))
  (testing "set"
    (is (= true (bm/contains-any? (bm/from {:x 1 :y 2})
                                  (bs/from #{:x :z}))))))

(deftest merge-test
  (testing "lww"
    (is (= {:x 1 :y 2 :z 3}
           (datafy (bm/merge (bm/from {:x 1 :y 1})
                             (bm/from {:y 2 :z 3}))))))

  (testing "merge-fn"
    (is (= {:x 1 :y 3 :z 3}
           (datafy (bm/merge (bm/from {:x 1 :y 1})
                             (bm/from {:y 2 :z 3})
                             +))))))

(deftest difference-test
  (testing "map"
    (is (= {:x 1}
           (datafy (bm/difference (bm/from {:x 1 :y 1})
                                  (bm/from {:y 2 :z 2}))))))
  (testing "set"
    (is (= {:x 1}
           (datafy (bm/difference (bm/from {:x 1 :y 1})
                                  (bs/from #{:y :z})))))))

(deftest difference-test
  (is (= {:x 1 :y 2 :z 2}
         (datafy (bm/union (bm/from {:x 1 :y 1})
                           (bm/from {:y 2 :z 2}))))))

(deftest intersection-test
  (testing "map"
    (is (= {:y 1}
           (datafy (bm/intersection (bm/from {:x 1 :y 1})
                                    (bm/from {:y 2 :z 2}))))))
  (testing "set"
    (is (= {:y 1}
           (datafy (bm/intersection (bm/from {:x 1 :y 1})
                                    (bs/from #{:y :z})))))))

(deftest put-test
  (is (= {:x 2} (-> (bm/map)
                    (bm/put :x 1)
                    (bm/put :x 2)
                    datafy))))

(deftest update-test
  (let [m (bm/from {:x 1})]
    (is (= {:x 2} (-> m (bm/update :x inc) datafy)))
    (is (= {:x 5} (-> m (bm/update :x + 4) datafy)))))

(deftest remove-test
  (is (= {:x 1} (-> {:x 1 :y 2} bm/from (bm/remove :y) datafy))))
