(ns bifurcan-clj.list-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is]]]
            [bifurcan-clj [core :as b]
                                    [list :as bl]]))

(deftest empty-test
  (is (not (b/linear? bl/empty)))
  (is (= 0 (b/size bl/empty)))
  (is (= [] (datafy bl/empty))))

(deftest list-test
  (is (= [1 2 3] (datafy (bl/list 1 2 3)))))

(deftest from-test
  (is (= [1 2 3] (datafy (bl/from [1 2 3]))))
  (is (= [1 2 3] (datafy (bl/from (.iterator [1 2 3]))))))

(deftest add-first-last-test
  (is (= [1 2 3 4]
         (-> bl/empty
            (bl/add-first 2)
            (bl/add-last 3)
            (bl/add-first 1)
            (bl/add-last 4)
            datafy))))

(deftest set-test
  (is (= [1] (-> bl/empty (bl/set 0 1) datafy)))
  (is (= [1 :x 3] (-> (bl/list 1 2 3) (bl/set 1 :x) datafy))))

(deftest nested-test
  (is (= [[1 2] [3 4]]
         (datafy
           (bl/list
             (bl/list 1 2)
             (bl/list 3 4))))))

(deftest slice-test
  (is (= [2 3]
         (datafy (bl/slice (bl/list 1 2 3 4) 1 3)))))

(deftest first-test
  (is (= 1 (bl/first (bl/list 1 2 3)))))

(deftest last-test
  (is (= 3 (bl/last (bl/list 1 2 3)))))
(deftest concat-test
  (is (= [1 2 3] (datafy (bl/concat (bl/list 1) (bl/list 2 3))))))

(deftest concat-all-test
  (is (= [1 2 3 4] (datafy (bl/concat-all
                             [(bl/list 1)
                              (bl/list 2 3)
                              (bl/list 4)])))))

(deftest sort-test
  (is (= [1 2 3] (datafy (bl/sort (bl/list 3 2 1))))))

(deftest reverse-test
  (is (= [3 2 1] (datafy (bl/reverse (bl/list 1 2 3))))))
