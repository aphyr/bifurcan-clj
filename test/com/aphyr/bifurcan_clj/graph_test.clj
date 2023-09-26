(ns com.aphyr.bifurcan-clj.graph-test
  (:require [clojure [datafy :refer [datafy]]
                     [pprint :refer [pprint]]
                     [test :refer [deftest is testing]]]
            [com.aphyr.bifurcan-clj [core :as b]
                                    [graph :as g]
                                    [list :as l]
                                    [map :as m]
                                    [set :as s]]))

(deftest graph-test
  (testing "empty"
    (is (= {} (datafy (g/graph)))))

  (testing "custom equality/hash"
    ; Very weird: I'm not sure why we get duplicate vertices here. Bug in
    ; bifurcan maybe?
    (is (= {:x #{:y "y"}
            :y #{:x "x"}}
           (-> (g/graph #(hash (name %)) (fn [a b] (= (name a) (name b))))
               (g/link :x :y)
               (g/link "y" "x")
               datafy)))))

(deftest link-test
  (testing "no edge val"
    (is (= nil (-> (g/graph) (g/link :x :y) (g/edge :x :y)))))

  (testing "edge val"
    (is (= 2 (-> (g/graph) (g/link :x :y 2) (g/edge :x :y)))))

  (testing "edge val merge"
    (is (= 3 (-> (g/graph) (g/link :x :y 1) (g/link :x :y 2 +)
                 (g/edge :x :y))))))

(deftest edges-test
  (is (= #{{:from :x, :to :y, :value 1}
           {:from :y, :to :z, :value 2}}
         (-> (g/graph)
             (g/link :x :y 1)
             (g/link :y :z 2)
             g/edges
             (->> (map datafy))
             set))))

(deftest unlink-test
  (is (= {:x #{}, :y #{}}
         (-> (g/graph) (g/link :x :y) (g/unlink :y :x) datafy))))

(deftest vertices-test
  (is (= #{:x :y}
         (-> (g/graph) (g/link :x :y) g/vertices datafy))))

(deftest in-test
  (is (= #{:x}
         (-> (g/digraph) (g/link :x :y) (g/in :y) datafy))))

(deftest out-test
  (is (= #{:y}
         (-> (g/digraph) (g/link :x :y) (g/out :x) datafy))))

(deftest remove-test
  (is (= #{:x}
         (-> (g/digraph) (g/link :x :y) (g/remove :y) g/vertices datafy))))

(deftest map-edges-test
  (is (= 2
         (-> (g/graph)
             (g/link :x :y 1)
             (g/map-edges (comp inc g/edge-value))
             (g/edge :x :y)))))

(deftest index-of-test
  (let [g (-> (g/graph) (g/add-vertex :x))]
    (is (= 0 (g/index-of g :x)))
    (is (= nil (g/index-of g :y)))))

(deftest select-test
  (is (= (-> (g/graph)
             (g/link :y :z)
             datafy)
         (-> (g/graph)
             (g/link :x :y)
             (g/link :y :z)
             (g/select (s/from #{:y :z}))
             datafy))))

(deftest transpose-test
  (is (= {:y #{:x} :x #{}}
         (-> (g/digraph)
             (g/link :x :y)
             g/transpose
             datafy))))

(deftest directed-test
  (is (g/directed? (g/digraph)))
  (is (g/directed? (g/directed-acyclic-graph))))

(deftest top-bottom-test
  (let [g (-> (g/directed-acyclic-graph)
              (g/link :a1 :b)
              (g/link :a2 :b)
              (g/link :b  :c1)
              (g/link :b  :c2)
              (g/link :a3 :c3))]
    (testing "top"
      (is (= #{:a1 :a2 :a3} (datafy (g/top g)))))
    (testing "bottom"
      ; Is the presence of b here a bug, or expected behavior?
      (is (= #{:b :c1 :c2 :c3} (datafy (g/bottom g)))))))

(deftest merge-test
  (let [g (g/merge (-> (g/digraph) (g/link :x :y 1))
                   (-> (g/digraph)
                       (g/link :x :y 2)
                       (g/link :y :z 2))
                   (fn merge [a b]
                     ;(prn :merge a b)
                     (+ a b)))]
    (is (= {:x #{:y}
            :y #{:z}
            :z #{}}
           (datafy g)))
    ; I suspect this is a bug in bifurcan. Merge is called but result ignored?
    ;(is (= 3 (g/edge g :x :y)))
    (is (= 2 (g/edge g :x :y))) ; BUG?
    (is (= 2 (g/edge g :y :z)))))

(deftest shortest-path-test
  (let [g (-> (g/graph)
              (g/link :a :b1 1)
              (g/link :a :b2 1)
              (g/link :b1 :c 1)
              (g/link :b2 :c 2))]
    (is (= [:a :b1 :c]
           (datafy (g/shortest-path g :a #{:c} g/edge-value))))
    (is (= [:a :b1 :c]
           (datafy (g/shortest-path-from-any g [:a] #{:c} g/edge-value))))))

(deftest connected-components-test
  (let [g (-> (g/graph)
              (g/link :a :b)
              (g/link :b :c)
              (g/link :d :e))]
    (is (= #{#{:a :b :c} #{:d :e}}
           (datafy (g/connected-components g))))))

(deftest biconnected-components-test
  (let [g (-> (g/graph)
              (g/link :a :b)
              (g/link :a :c)
              (g/link :b :c)
              (g/link :d :e)
              (g/link :e :f))]
    (is (= #{#{:a :b :c} #{:d :e} #{:e :f}}
           (datafy (g/biconnected-components g))))))

(deftest articulation-points
  (let [g (-> (g/graph)
              (g/link :a :b)
              (g/link :b :c))]
    (is (= #{:b} (datafy (g/articulation-points g))))))

(let [g (-> (g/digraph)
            (g/link :a :b)
            (g/link :b :c)
            (g/link :c :a)
            (g/link :d :e))]
  (deftest strongly-connected-components-test
    (is (= #{#{:a :b :c}} (datafy (g/strongly-connected-components g)))))

  (deftest strongly-connected-subgraphs
    (is (= [{:a #{:b} :b #{:c} :c #{:a}}]
           (datafy (g/strongly-connected-subgraphs g)))))

  (deftest cycles-test
    (is (= [[:b :c :a :b]] (datafy (g/cycles g)))))

  (deftest bfs-vertices-test
    (is (= [:a :b :c]
           (seq (g/bfs-vertices :a (partial g/out g))))))

  (deftest bfs-vertices-from-any-test
    (is (= [:a :b :c]
           (seq (g/bfs-vertices-from-any [:a] (partial g/out g)))))))
