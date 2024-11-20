(ns bifurcan-clj.util
  "Utility functions"
  (:refer-clojure :exclude [])
  (:require [bifurcan-clj.core :as b]
            [clojure [core :as c]
                     [datafy :refer [datafy]]]
            [clojure.core [protocols :as p]])
  (:import (io.lacuna.bifurcan.utils Iterators)))

(defn iterator=
  "Compares two iterators for equality. Uses Clojure = by default, but can take
  any function."
  ([a b]
   (iterator= a b =))
  ([a b =]
   (Iterators/equals a b (b/functional =))))

(defn iterable=
  "Compares two iterable things for equality. This is helpful for (e.g.)
  comparing a vector to a List. Takes an optional Clojure function for
  equality."
  ([a b]
   (iterable= a b =))
  ([^Iterable a, ^Iterable b =]
   (iterator= (.iterator a) (.iterator b) =)))
