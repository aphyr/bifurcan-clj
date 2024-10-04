(ns bifurcan-clj.set
  "Functions for working with sets."
  (:refer-clojure :exclude [contains?
                            empty
                            get
                            keys
                            map
                            remove
                            set])
  (:require [clojure [core :as c]]
            [bifurcan-clj.core :refer [functional]])
  (:import (java.util Iterator
                      OptionalLong)
           (io.lacuna.bifurcan IList
                               IMap
                               ISet
                               Set)))

(def ^Set empty
  "The empty set."
  Set/EMPTY)

(defn ^Set set
  "Constructs a new Set. Optionally takes a function for hashing and a
  functionf or equality."
  ([]
   (Set.))
  ([hash-fn equals-fn]
   (Set. (functional hash-fn) (functional equals-fn))))

(declare add)

(defprotocol From
  (from ^io.lacuna.bifurcan.ISet [x] "Coerces x to a set."))

(extend-protocol From
  nil
  (from [_] empty)

  clojure.lang.IReduceInit
  (from [xs]
    (.forked ^ISet
      (reduce add (.linear empty) xs)))

  clojure.lang.Seqable
  (from [xs]
    (.forked ^ISet
      (reduce add (.linear empty) xs)))

  Iterable
  (from [it] (Set/from it))

  Iterator
  (from [it] (Set/from it))

  IList
  (from [l] (Set/from l)))

(defn value-hash
  "The hash function used by the set"
  [^ISet s]
  (.valueHash s))

(defn value-equality
  "The equality semantics used by the set"
  [^ISet s]
  (.valueEquality s))

(defn contains?
  "Does the set contain x?"
  [^ISet s, x]
  (.contains s x))

(defn ^IList elements
  "A list of all elements in the set."
  [^ISet s]
  (.elements s))

(defn ^IMap zip
  "Constructs a map which has a corresponding value (f x) for each x in the set
  xs."
  [^ISet xs, f]
  (.zip xs (functional f)))

(defn index-of
  "Returns a the index of the given element in the set, or nil if it's absent."
  [^ISet xs, x]
  (let [^OptionalLong i (.indexOf xs x)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn contains-all?
  "True if this set contains all keys in the given map or set."
  [^ISet s, other]
  (condp instance? other
    IMap (.containsAll s ^IMap other)
    ISet (.containsAll s ^ISet other)
    (throw (IllegalArgumentException. "must take an IMap or ISet"))))

(defn contains-any?
  "True if this set contains any keys in the given map or set."
  [^ISet s, other]
  (condp instance? other
    IMap (.containsAny s ^IMap other)
    ISet (.containsAny s ^ISet other)
    (throw (IllegalArgumentException. "must take an IMap or ISet"))))

(defn ^ISet add
  "Adds an element to the set."
  [^ISet xs x]
  (.add xs x))

(defn ^ISet remove
  "Removes an element from the set."
  [^ISet xs x]
  (.remove xs x))

(defn ^ISet union
  "Takes the union of two sets."
  [^ISet a, ^ISet b]
  (.union a b))

(defn ^ISet difference
  "Subtracts all elements of b from a."
  [^ISet a, ^ISet b]
  (.difference a b))

(defn ^ISet intersection
  "Takes the intersection of sets a and b."
  [^ISet a, ^ISet b]
  (.intersection a b))
