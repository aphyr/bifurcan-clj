# Bifurcan for Clojure

[![Clojars Project](https://img.shields.io/clojars/v/com.aphyr/bifurcan-clj.svg)](https://clojars.org/com.aphyr/bifurcan-clj)

[Bifurcan](https://github.com/lacuna/bifurcan) is an excellent library of
high-performance mutable/persistent data structures by Zach Tellman. However,
using Bifurcan structures from Clojure involves a bit of type hinting, reifying
Java function interfaces, and so on--not much, but just enough to be
cumbersome. This library provides idiomatic Clojure wrappers for Bifurcan with
an emphasis on performance.

## Usage

The library is split into several namespaces, one for each major datatype.
Common functions are in `core`.

```clj
(require '[bifurcan-clj
           [core :as b]
           [list :as l]
           [set :as s]
           [map :as m]
           [int-map :as im]
           [graph :as g]])
```

Working with lists.

```clj
; You can construct a list by hand
(l/list 1 2 3)
#object[io.lacuna.bifurcan.List 0x59e107e4 "[1, 2, 3]"]

; Or turn any iterable into a list:
(l/from (map inc [1 2 3]))
#object[io.lacuna.bifurcan.List 0x7f7fe6e "[2, 3, 4]"]

; You can use datafy to turn Bifurcan structures back into Clojure structures.
(datafy (l/list 1 2 3))
[1 2 3]

; Quickly build up a list using local mutability, then convert it back to an
; immutable structure
(-> (l/list 1 2 3) b/linear (l/add-first :x) (l/add-last :y) b/forked datafy)
[:x 1 2 3 :y]

; You can ask for the nth or size of any collection.
(b/nth (l/list 1 2 3) 1)
2

(b/size (l/list 1 2 3))
3

; Take a slice of the list
(-> (range 100) l/from (l/slice 10 20) datafy)
[10 11 12 13 14 15 16 17 18 19]
```

Working with maps.

```clj
; Maps are constructed with `from`:
(def m (m/from {:x 1 :y 2}))
#object[io.lacuna.bifurcan.Map 0x46c58798 "{:y 2, :x 1}"]

; Read an element
(m/get m :x)
1

(m/get m :z)
nil

(m/get m :z :not-found)
:not-found

; Set an element
(datafy (m/put m :z 3))
{:y 2, :x 1, :z 3}

; put can take an optional merge function to combine with an extant value
(datafy (m/put m :y 3 +))
{:y 5, :x 1}

; You can update a value in place by applying a function
(datafy (m/update m :y + 3))
{:y 5, :x 1}

; Map support union, intersection, difference, and so on.
(datafy (m/union m (m/from {:y 5 :z 10})))
{:y 5, :x 1, :z 10}
```

; Maps can also transform keys to integer indices
(m/index-of m :x)
1

com.aphyr.bifurcan-clj.core=> (b/nth m 1)
#object[io.lacuna.bifurcan.Maps$Entry 0x63cac402 ":x = 1"]
```

Bifurcan sets and graphs work too. For instance, here's how to find
articulation points of a graph:

```clj
; If we were to remove b1, it would partition the graph in two
(-> (g/graph)
    (g/link :a1 :b1)
    (g/link :a2 :b1)
    (g/link :b1 :c1)
    (g/link :b1 :c2)
    g/articulation-points
    datafy)
#{:b1}
```

See [the tests](test/com/aphyr/bifurcan_clj/) for detailed examples.

## Features

This library extends Bifurcan structures to support Clojure's
[datafy](https://clojuredocs.org/clojure.datafy/datafy) protocol. This makes it
possible to work with a Bifurcan structure for speed, then coerce it
back to an idiomatic Clojure structure when desired.

## Philosophy

If you're using Bifurcan you probably care about performance, so these wrappers
are designed to be inliner-friendly and to avoid reflection and manual type
checks wherever possible.

Common functions live in `com.aphyr.bifurcan-clj.core`. Functions for a
specific datatype (and its associated classes) are in their own namespace:
graphs and vertices live in `com.aphyr.bifurcan-clj.graph`, maps and entries in
`com.aphyr.bifurcan-clj.map`, and so on.

Coercion between Clojure and Bifurcan datatypes is generally
explicit, rather than implicit. If a function needs a list as an argument, it
generally takes a Bifurcan `IList`, rather than also supporting a Clojure seq.
We do this to keep functions small, predictable, and to avoid branching in
potentially hot codepaths.

There are a few exceptions to this rule. Many functions in Bifurcan use a Java
functional interface like `BiPredicate`. In this library you provide a Clojure
function (or map, or set, etc), and we lift it (using
`bifurcan-clj.core/functional`) into a wrapper that satisfies `BiPredicate`,
`Consumer`, etc.

We also return `nil` instead of Optionals pretty much everywhere. This is
generally unambiguous, and where it might be, there are explicit not-found
paths. Clojure is great with nil, whereas working with OptionalLong can be a
bit cumbersome.

Clojure lacks Java's argument type dispatch. Where argument types would be
ambiguous and you might want to control which is used, we generally provide
multiple explicit functions with different names: `graph/shortest-path`,
`graph/shortest-path-from-any`. In some cases (e.g. constructors) we use
`instanceof` checks or protocol polymorphism: `list/from` supports iterables,
iterators, and lists.

We generally use Bifurcan's function names rather than their Clojure
equivalents. We use `forked` and `linear` rather than `persistent!` and
`transient`. Counting a collection is done with `(b/size coll)`, since the
underlying method is `(.size coll)`.

## Status

There's reasonably complete support for lists, sets, maps, intmaps, and all
three kinds of graphs. I haven't done split/merge, Java iteration, diffs, or
durable structures.

I'm starting with the parts of the Bifurcan API I use the most often, but every
part of Bifurcan should eventually be in scope. If there are structures or
functions you're missing, please feel free to open a PR.

## License

Copyright © 2023 Kyle Kingsbury

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
