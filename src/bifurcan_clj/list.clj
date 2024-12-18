(ns bifurcan-clj.list
  "Functions for working with lists."
  (:refer-clojure :exclude [concat
                            empty
                            first
                            last
                            list
                            reverse
                            set
                            sort])
  (:import (java.util Comparator
                      Iterator)
           (io.lacuna.bifurcan IList
                               List
                               Lists)))

(def ^List empty
  "The empty list."
  List/EMPTY)

(declare add-last)

(defn list
  "Constructs a new list. With arguments, constructs a list of those
  arguments."
  ([]
   List/EMPTY)
  ([& args]
   (List/from ^Iterable args)))

(defprotocol From
  (from ^io.lacuna.bifurcan.IList [x] "Coerces x to a list."))

(extend-protocol From
  ; We want to be able to convert Clojure maps and lazy sequences of [k v]
  ; vector pairs to IntMaps readily. These are our paths for that coercion.
  nil
  (from [_] empty)

  clojure.lang.IReduceInit
  (from [xs]
    (.forked ^List
      (reduce add-last (.linear empty) xs)))

  clojure.lang.Seqable
  (from [xs]
    (.forked ^List
      (reduce add-last (.linear empty) xs)))

  Iterable
  (from [x] (List/from ^Iterable x))
  Iterator
  (from [x] (List/from ^Iterator x))
  IList
  (from [x] (List/from ^IList x)))

(defn ^IList from-array
  "Like `(from xs)`, constructs a list from an array. Extending protocols over
  array subtypes is... maybe impossible."
  [^objects xs]
  (Lists/from xs))

(defn ^IList add-first
  "Adds an entry at the front of the list."
  [^IList xs, x]
  (.addFirst xs x))

(defn ^IList add-last
  "Adds an entry at the end of the list."
  [^IList xs, x]
  (.addLast xs x))

(defn set
  "Returns a new list, with the element at idx overwritten with value. If idx
  is equal to ICollection.size(), the value is appended."
  [^IList xs, ^long n, x]
  (.set xs n x))

(defn ^IList slice
  "Takes a sub-range of the given list from start (inclusive) to end
  (exclusive). Linear if this list is linear."
  [^IList xs, ^long start, ^long end]
  (.slice xs start end))

(defn first
  "The first entry in the list."
  [^IList xs]
  (.first xs))

(defn last
  "The last entry in the list."
  [^IList xs]
  (.last xs))

(defn ^IList concat-all
  "Concatenates a collection of lists."
  [lists]
  (Lists/concat (into-array IList lists)))

(defn ^IList concat
  "Concatenates two lists."
  [^IList a, ^IList b]
  (Lists/concat a b))

(defn ^IList sort
  "Sorts a list, optionally with a comparator."
  ([^IList xs]
   (Lists/sort xs))
  ([^IList xs, ^Comparator comparator]
   (Lists/sort xs comparator)))

(defn ^IList reverse
  "Reverses a list."
  [^IList xs]
  (Lists/reverse xs))
