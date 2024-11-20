(ns bifurcan-clj.util-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [bifurcan-clj [core :as b]
                          [list :as l]
                          [map :as m]
                          [set :as s]
                          [util :refer :all]]))

(deftest iterator=-test
  (is (not (iterator= (.iterator [1 2 3]) (.iterator [1 2]))))
  (is      (iterator= (.iterator [1 2 3]) (.iterator [1 2 3]))))
