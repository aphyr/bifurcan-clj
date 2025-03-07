(ns bifurcan-clj.int-map
  "Functions for working with IntegerMaps specifically: an efficient,
  persistent data structure mapping longs to objects. See also
  bifurcan-clj.map for general-purpose map operations."
  (:refer-clojure :exclude [comparator
                            contains?
                            empty
                            get
                            key
                            keys
                            map
                            merge
                            nth
                            update
                            remove
                            ])
  (:require [clojure [core :as c]]
            [bifurcan-clj [core :refer [functional]]
             [map :as m]])
  (:import (bifurcan_clj.core FunctionalN)
           (java.util Collection
                      Comparator
                      Iterator
                      OptionalLong)
           (java.util.function BiFunction
                               ToLongFunction)
           (io.lacuna.bifurcan IEntry
                               IList
                               IMap
                               ISet
                               IntMap
                               Map
                               Maps)))

(defprotocol From
  (from ^io.lacuna.bifurcan.IntMap [x]
        "Constructs an map from another collection. Can take a Bifurcan map, a
        Bifurcan list of Bifurcan Entries, or an iterable of anything that can
        be coerced to an Entry--Clojure map entries, [k v] vector pairs, or
        Bifurcan entries."))

(defn ^IntMap reducible->int-map
  "Takes any reducible series of pairs (e.g. [k v] vectors, clojure map
  entries, Bifurcan map entries) and converts it to an IntMap."
  [pairs]
  (.forked ^IntMap
           (reduce (fn [^IntMap m, pair]
                     (let [e (m/->entry pair)]
                       (.put m (.key e) (.value e))))
                   (IntMap.)
                   pairs)))

; This lets us re-use the same fn across multiple classes
(extend clojure.lang.IReduceInit From {:from reducible->int-map})
(extend clojure.lang.Seqable     From {:from reducible->int-map})

(extend-protocol From
  ; We want to be able to convert Clojure maps and lazy sequences of [k v]
  ; vector pairs to IntMaps readily. These are our paths for that coercion.
  nil
  (from [_] (IntMap.))

  ; From a collection of IEntries
  Collection
  (from [entries]
        (if (instance? java.util.Map$Entry (first entries))
          (IntMap/from entries)
          (reducible->int-map entries)))

  IMap
  (from [m] (IntMap/from m))

  Map
  (from [m] (IntMap/from m)))

(defn ^IntMap int-map
  "Constructs an integer map."
  []
  (IntMap.))

(defn ^Comparator comparator
  "The comparator of this map."
  [^IntMap m]
  (.comparator m))

(defn ^IntMap slice
  "A map representing all entries within [min max] inclusive."
  [^IntMap m, ^long min, ^long max]
  (.slice m min max))

(defn get
  "Gets a key from a map. Returns the value, or nil if not found. Can take an
  explicit not-found value."
  ([^IntMap m, ^long k]
   (.orElse (.get m k) nil))
  ([^IntMap m, ^long k, not-found]
   (.get m k not-found)))

(defn contains?
  "Does the given map contain a key?"
  [^IntMap m, ^long k]
  (.contains m k))

(defn ^IList entries
  "Returns a list of entries in the map."
  [^IMap m]
  (.entries m))

(defn ^IEntry nth
  "The entry at index i."
  [^IntMap m, ^long i]
  (.nth m i))

(defn index-of
  "Returns the Long index of a key, if present, or nil."
  [^IMap m, ^long k]
  (let [i (.indexOf m k)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn floor-index
  "The index of the entry whose key is either equal to key, or just below it.
  If key is less than the minimum value in the map, returns null."
  [^IntMap m, ^long k]
  (let [i (.floorIndex m k)]
    (when (.isPresent i)
      (.getAsLong i))))

;(defn inclusive-floor-index
;  "Not sure what this is."
;  [^IntMap m, k]
;  (let [i (.inclusiveFloorIndex m k)]
;    (when (.isPresent i)
;      (.getAsLong i))))

(defn ceil-index
  "The index of the entry whose key is either equal to key, or just above it.
  If key is greater than the maximum value in the map, returns null."
  [^IntMap m, ^long k]
  (let [i (.ceilIndex m k)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn ^IntMap put
  "Sets a key to a value. Optionally takes a function (merge current-val
  new-val) to combine an existing value."
  ([^IntMap m, ^long k, v]
   (.put m k v))
  ([^IntMap m, ^long k, v, merge]
   (.put m k v (functional merge))))

(defn ^IntMap update
  "Updates a key by applying a function to the current value, or nil if it does
  not exist."
  ([^IntMap m, ^long k, f]
   (.update m k (functional f)))
  ([^IntMap m, ^long k, f a]
   (.update m k (functional f a)))
  ; Can't type-hint 4+ arg fns
  ([^IntMap m, k, f a b]
   (.update m k (functional f a b)))
  ([^IntMap m, k, f a b & more]
   (.update m k ^FunctionalN (FunctionalN. f a b more))))

(defn ^IntMap remove
  "Removes a key from the map."
  [^IntMap m, ^long k]
  (.remove m k))
