(ns bifurcan-clj.linear-list-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is]]]
            [bifurcan-clj [core :as b]
                          [list :as bl]
                          [linear-list :as bll]]))

(deftest empty-test
  (is (b/linear? (bll/linear-list)))
  (is (= 0 (b/size (bll/linear-list))))
  (is (= [] (datafy (bll/linear-list)))))

(deftest linear-list-test
  (is (= [] (datafy (bll/linear-list 3)))))

(deftest from-test
  (is (= [1 2 3] (datafy (bll/from [1 2 3]))))
  (is (= [2 3 4] (datafy (bll/from (map inc [1 2 3])))))
  (is (= [1 2 3] (datafy (bll/from (.iterator [1 2 3]))))))

(deftest clear-test
  (let [l (-> (bll/from [1 2 3])
              (bll/clear))]
    (is (= [] (datafy l)))))

(deftest pop-first-test
  (let [l (bll/from [1 2 3])]
    (is (= 1 (bll/pop-first l)))
    (is (= [2 3] (datafy l)))
    (is (= 2 (bll/pop-first l)))
    (is (= [3] (datafy l)))))

(deftest pop-last-test
  (let [l (bll/from [1 2 3])]
    (is (= 3 (bll/pop-last l)))
    (is (= [1 2] (datafy l)))
    (is (= 2 (bll/pop-last l)))
    (is (= [1] (datafy l)))))
