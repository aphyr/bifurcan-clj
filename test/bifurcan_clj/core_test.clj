(ns bifurcan-clj.core-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [bifurcan-clj [core :as b]
                          [list :as l]
                          [map :as m]
                          [set :as s]]))

(deftest linear-forked-test
  (let [l (l/list 1 2 3)
        _ (is (not (b/linear? l)))
        l (b/linear l)
        _ (is (b/linear? l))
        l (l/add-first l :x)
        l (b/forked l)
        _ (is (not (b/linear? l)))
        _ (is (= [:x 1 2 3] (datafy l)))]))

(deftest size-test
  (is (= 0 (b/size m/empty))))
