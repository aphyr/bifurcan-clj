(ns bifurcan-clj.core
  "Common functions across all Bifurcan collections."
  (:refer-clojure :exclude [key
                            nth])
  (:require [clojure [core :as c]
                     [datafy :refer [datafy]]]
            [clojure.core [protocols :as p]
                          [reducers :as r]])
  (:import (io.lacuna.bifurcan ICollection
                               IEdge
                               IEntry
                               IGraph
                               IList
                               IMap
                               ISet)
           (java.util.function BiFunction
                               BiPredicate
                               BinaryOperator
                               Consumer
                               Function
                               Predicate
                               Supplier
                               ToDoubleFunction
                               ToLongFunction
                               UnaryOperator
                               )))

(defn ^ICollection clone
  "A shallow copy of a collection."
  [^ICollection coll]
  (.clone coll))

(defn ^ICollection linear
  "This returns a data structure which is linear, or temporarily mutable."
  [^ICollection coll]
  (.linear coll))

(defn ^ICollection forked
  "This returns a data structure which is forked, which is equivalent to
  Clojure's persistent data structures, also sometimes called functional or
  immutable."
  [^ICollection coll]
  (.forked coll))

(defn ^ICollection linear?
  "Is a collection linear?"
  [^ICollection coll]
  (.isLinear coll))

(defn nth
  "Like Clojure nth, returns the nth element of a collection. Takes an optional
  not-found value."
  ([^ICollection coll ^long n]
   (.nth coll n))
  ([^ICollection coll ^long n not-found]
   (.nth coll n not-found)))

(defn ^long size
  "How large is a collection?"
  [^ICollection coll]
  (.size coll))

(defn ^IList split
  "Splits the collection into roughly even pieces, for parallel processing."
  [^ICollection coll, ^long parts]
  (.split coll parts))

;; Coercion back to Clojure structures.

(extend-protocol p/Datafiable
  IList
  (datafy [xs]
    (mapv datafy xs))

  IEntry
  (datafy [entry]
    (clojure.lang.MapEntry. (datafy (.key entry))
                            (datafy (.value entry))))

  IEdge
  (datafy [edge]
    {:from  (datafy (.from edge))
     :to    (datafy (.to edge))
     :value (datafy (.value edge))})

  IMap
  (datafy [s]
    (let [iter (.iterator s)]
      (loop [m (transient {})]
        (if (.hasNext iter)
          (let [kv ^IEntry (.next iter)]
            (recur (assoc! m
                           (datafy (.key kv))
                           (datafy (.value kv)))))
          (persistent! m)))))

  ISet
  (datafy [s]
    (let [iter (.iterator s)]
    (loop [s (transient #{})]
      (if (.hasNext iter)
        (recur (conj! s (datafy (.next iter))))
        (persistent! s)))))

  IGraph
  (datafy [g]
    (->> (.vertices g)
         (r/map (fn [vertex] [(datafy vertex) (datafy (.out g vertex))]))
         (into {}))))

;; Java functional interface wrappers.
(deftype Functional [f]
  BiFunction
  (apply [_ x y] (f x y))

  BiPredicate
  (test [_ x y] (f x y))

  BinaryOperator

  Consumer
  (accept [_ x] (f x))

  Supplier
  (get [_] (f))

  Predicate
  (test [_ x] (boolean (f x)))

  Function
  (apply [_ x] (f x))

  ToLongFunction
  (applyAsLong [_ x] (f x))

  ToDoubleFunction
  (applyAsDouble [_ x] (f x))

  UnaryOperator)

; Curried variants
(deftype Functional1 [f a]
  UnaryOperator
  Function
  (apply [_ x] (f x a)))

(deftype Functional2 [f a b]
  UnaryOperator
  Function
  (apply [_ x] (f x a b)))

(deftype FunctionalN [f a b more]
  UnaryOperator
  Function
  (apply [_ x] (apply f x a b more)))

(defn functional
  "Wraps a Clojure function (or anything that works like a function, like sets
  and maps) in a Java functional wrapper that implements Supplier, Function,
  BiFunction, Predicate, etc. Functions can do anything if they believe in
  themselves.

  If additional args a b ... are provided, produces curried functions which
  call (f x a b ...)."
  ([f]
   (Functional. f))
  ([f a]
   (Functional1. f a))
  ([f a b]
   (Functional2. f a b))
  ([f a b & more]
   (FunctionalN. f a b more)))
