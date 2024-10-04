(ns bifurcan-clj.linear-list
  "Functions for working with linear lists, which are optimized for linear use,
  rather than efficient forking."
  (:refer-clojure :exclude [concat
                            empty
                            first
                            last
                            list
                            reverse
                            set
                            sort])
  (:require [bifurcan-clj [list :as bl]])
  (:import (java.util Comparator
                      Collection
                      Iterator)
           (io.lacuna.bifurcan IList
                               LinearList
                               Lists)))

(defn linear-list
  "Constructs a new linear list. Optionally takes a capacity."
  ([]
   (LinearList.))
  ([^long capacity]
   (LinearList. capacity)))

(defprotocol From
  (from ^io.lacuna.bifurcan.LinearList [x] "Coerces x to a linear list."))

(extend-protocol From
  nil
  (from [_] (linear-list))

  clojure.lang.IReduceInit
  (from [xs]
    (reduce bl/add-last (linear-list) xs))

  clojure.lang.Seqable
  (from [xs]
    (reduce bl/add-last (linear-list) xs))

  Iterable
  (from [x] (LinearList/from ^Iterable x))
  Iterator
  (from [x] (LinearList/from ^Iterator x))
  IList
  (from [x] (LinearList/from ^IList x))
  Collection
  (from [x] (LinearList/from ^Collection x)))

(defn ^LinearList clear
  "Clears the contents of a linear list."
  [^LinearList xs]
  (.clear xs))

(defn pop-first
  "Removes and returns the first element of the list."
  [^LinearList xs]
  (.popFirst xs))

(defn pop-last
  "Removes and returns the last element of the list."
  [^LinearList xs]
  (.popLast xs))
