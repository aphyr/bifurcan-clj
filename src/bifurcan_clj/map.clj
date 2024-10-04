(ns bifurcan-clj.map
  "Functions for working with sorted and hash maps."
  (:refer-clojure :exclude [comparator
                            contains?
                            empty
                            first
                            get
                            key
                            keys
                            last
                            map
                            merge
                            update
                            remove
                            sorted-map
                            ])
  (:require [clojure [core :as c]]
            [bifurcan-clj.core :refer [functional ->FunctionalN]])
  (:import (java.util Comparator
                      Iterator
                      OptionalLong)
           (java.util.function ToLongFunction)
           (io.lacuna.bifurcan ICollection
                               IDiffSortedMap
                               IEntry
                               IList
                               IMap
                               ISet
                               ISortedMap
                               ISortedSet
                               Map
                               Maps
                               SortedMap)))

(def empty
  "The empty map."
  Map/EMPTY)

(defprotocol ToEntry
  (^IEntry ->entry [pair] "Coerces a pair to an entry."))

(extend-protocol ToEntry
  IEntry
  (->entry [entry] entry)

  clojure.lang.MapEntry
  (->entry [entry] (IEntry/of (c/key entry) (c/val entry)))

  clojure.lang.PersistentVector
  (->entry [[k v]] (IEntry/of k v)))

(defprotocol From
  (from ^io.lacuna.bifurcan.IMap [x]
        "Constructs a map from another collection. Can take a Bifurcan map, a
        Bifurcan list of Bifurcan Entries, or an iterable/reducible/seqable of
        anything that can be coerced to an Entry--Clojure map entries, [k v]
        vector pairs, or Bifurcan entries.")

  (sorted-map-from ^io.lacuna.bifurcan.ISortedMap [x]
                   "Constructs a sorted map from another collection. Can take a
                   Bifurcan map or an iterable/reducible/seqable of anything
                   that can be coerced to an Entry--Clojure map entries, [k v]
                   vector pairs, or Bifurcan entries."))

(defn from-reduce
  "Takes an initial map and a reducible of things that can be coerced to
  entries. Adds all those entries to the map and returns the map, forked."
  [^IMap init pairs]
  (.forked ^ICollection
           (reduce (fn [^IMap m, pair]
                     (let [e (->entry pair)]
                       (.put m (.key e) (.value e))))
                   (.linear init)
                   pairs)))

(extend-protocol From
  ; We want to be able to convert Clojure maps and lazy sequences of [k v]
  ; vector pairs to Bifurcan maps readily. This is our path for that coercion.
  nil
  (from [_] empty)
  (sorted-map-from [_] (SortedMap.))

  clojure.lang.IReduceInit
  (from [pairs]
    (from-reduce empty pairs))

  (sorted-map-from [pairs]
    (from-reduce (SortedMap.) pairs))

  clojure.lang.Seqable
  (from [pairs]
    (from-reduce empty pairs))

  (sorted-map-from [pairs]
    (from-reduce (SortedMap.) pairs))

  ; The direct list form expects IEntrys
  IList
  (from [l] (Map/from l))

  (sorted-map-from [l]
    (from-reduce (SortedMap.) l))

  ; The direct iterator form expects IEntrys
  Iterator
  (from [iter] (Map/from iter))

  (sorted-map-from [pairs]
    (from-reduce (SortedMap.) pairs))

  IMap
  (from [m] (Map/from m))

  (sorted-map-from [m]
    (sorted-map-from (.entries m)))

  Map
  (from [m] (Map/from m))

  (sorted-map-from [m]
    (SortedMap/from m)))

(defn ^Map map
  "Constructs an empty map. optionally takes a hash and equality function."
  ([]
   (Map.))
  ([hash-fn equals-fn]
   (Map. (functional hash-fn) (functional equals-fn))))

(defn sorted-map
  "Constructs an empty sorted map with the given comparator."
  ([]
   (SortedMap.))
  ([comparator]
   (SortedMap. comparator)))

(defn key
  "The key of a map entry."
  [^IEntry entry]
  (.key entry))

(defn value
  "The value of a map entry."
  [^IEntry entry]
  (.value entry))

(defn ^ToLongFunction key-hash
  "The key hash function used by this map."
  [^IMap m]
  (.keyHash m))

(defn ^ToLongFunction key-equality
  "The key equality function used by this map."
  [^IMap m]
  (.keyEquality m))

(defn get
  "Gets a key from a map. Returns the value, or nil if not found. Can take an
  explicit not-found value."
  ([^IMap m k]
   (.orElse (.get m k) nil))
  ([^IMap m k not-found]
   (.get m k not-found)))

(defn get-or-create
  "Gets a key, or a value generated by (f) if there is no such key."
  [^IMap m k f]
  (.getOrCreate m k (functional f)))

(defn contains?
  "Does the given map contain a key?"
  [^IMap m, k]
  (.contains m k))

(defn ^IList entries
  "Returns a list of entries in the map."
  [^IMap m]
  (.entries m))

(defn index-of
  "Returns the long index of a key, if present, or nil."
  [^IMap m k]
  (let [i (.indexOf m k)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn ^ISet keys
  "Returns the set of keys in the map."
  [^IMap m]
  (.keys m))

(defn ^IList values
  "Returns the list of values in the map."
  [^IMap m]
  (.values m))

; Not resolvable?
;(defn ^IMap slice-indices
;  "Returns a map with all entries with indices in [min max] inclusive."
;  [^IMap m ^long min ^long max]
;  (.sliceIndices ^Object m min max))

(defn ^IMap map-values
  "Transforms all values using a function (f k v)."
  [^IMap m, f]
  (.mapValues m (functional f)))

(defn contains-all?
  "True if this map contains all keys in the given map or set."
  [^IMap m, other]
  (condp instance? other
    IMap (.containsAll m ^IMap other)
    ISet (.containsAll m ^ISet other)
    (throw (IllegalArgumentException. "contains-all? must take an IMap or ISet"))))

(defn contains-any?
  "True if this map contains any keys in the given map or set."
  [^IMap m, other]
  (condp instance? other
    IMap (.containsAny m ^IMap other)
    ISet (.containsAny m ^ISet other)
    (throw (IllegalArgumentException. "contains-any? must take an IMap or ISet"))))

(defn ^IMap merge
  "Merges two maps together, optionally using the given merge function."
  ([^IMap a, b]
   (.merge a b Maps/MERGE_LAST_WRITE_WINS))
  ([^IMap a, b, merge-fn]
   (.merge a b (functional merge-fn))))

(defn ^IMap difference
  "Takes a map and removes all keys in the given set/map"
  [^IMap m, ks]
  (condp instance? ks
    ISet (.difference m ^ISet ks)
    IMap (.difference m ^IMap ks)
    (throw (IllegalArgumentException. "expects an IMap or ISet"))))

(defn ^IMap intersection
  "Takes a map and retains only the keys in the given set."
  [^IMap m, ^ISet ks]
  (condp instance? ks
    ISet (.intersection m ^ISet ks)
    IMap (.intersection m ^IMap ks)
    (throw (IllegalArgumentException. "expects an IMap or ISet"))))

(defn ^IMap union
  "The union of two maps, with values from m2 shadowing m1."
  [^IMap m1, ^IMap m2]
  (.union m1 m2))

(defn ^IMap put
  "Sets a key to a value. Optionally takes a function (merge current-val
  new-val) to combine an existing value."
  ([^IMap m k v]
   (.put m k v))
  ([^IMap m k v merge]
   (.put m k v (functional merge))))

(defn ^IMap update
  "Updates a key by applying a function to the current value, or nil if it does
  not exist. With additional arguments a b ..., calls (f current-value a b
  ...)."
  ([^IMap m, k, f]
   (.update m k (functional f)))
  ([^IMap m k f a]
   (.update m k (functional f a)))
  ([^IMap m k f a b]
   (.update m k (functional f a b)))
  ([^IMap m k f a b & more]
   (.update m k (->FunctionalN f a b more))))

(defn ^IMap remove
  "Removes a key from the map."
  [^IMap m, k]
  (.remove m k))

; Sorted maps

(defn ^Comparator comparator
  "Returns the comparator function for two keys."
  [^ISortedMap m]
  (.comparator m))

(defn floor-index
  "The index of the entry whose key is either equal to key, or just below it.
  If key is less than the minimum value in the map, returns null."
  [^ISortedMap m, k]
  (let [i (.floorIndex m k)]
    (when (.isPresent i)
      (.getAsLong i))))

;(defn inclusive-floor-index
;  "Not sure what this is."
;  [^ISortedMap m, k]
;  (let [i (.inclusiveFloorIndex m k)]
;    (when (.isPresent i)
;      (.getAsLong i))))

(defn ceil-index
  "The index of the entry whose key is either equal to key, or just above it.
  If key is greater than the maximum value in the map, returns null."
  [^ISortedMap m, ^long k]
  (let [i (.ceilIndex m k)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn ^IEntry floor
  "The entry whose key is either equal to k, or just below it. If k is less
  than the minimum value in the map, returns nil."
  ([^ISortedMap m k]
   (.floor m k))
  ; I don't think the bounds are accessible to us--maybe they need to be
  ; declared public?
  ;([^ISortedMap m k bound]
  ; (.floor m k bound))
  )

(defn ^IEntry ceil
  "The entry whose key is either equal to k, or just above it. If k is less
  than the minimum value in the map, returns nil."
  ([^ISortedMap m k]
   (.ceil m k))
  ;([^ISortedMap m k bound]
  ; (.ceil m k bound)))
  )

(defn ^IDiffSortedMap slice
  "Returns a sorted map with all entries with keys in [min max] inclusive."
  ([^ISortedMap m min max]
   (.slice m min max))
  ;([^ISortedMap m min min-bound max max-bound]
  ; (.slice m min min-bound max max-bound)))
  )

(defn ^IEntry first
  "The first entry of a sorted map"
  [^ISortedMap m]
  (.first m))

(defn ^IEntry last
  "The last entry of a sorted map"
  [^ISortedMap m]
  (.last m))
