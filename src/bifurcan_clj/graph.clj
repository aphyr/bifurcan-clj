(ns bifurcan-clj.graph
  "Functions for working with graphs."
  (:refer-clojure :exclude [merge
                            remove])
  (:require [clojure.core :as c]
            [bifurcan-clj.core :refer [functional]])
  (:import (io.lacuna.bifurcan DirectedGraph
                               DirectedAcyclicGraph
                               Graph
                               Graphs
                               Graphs$DirectedEdge
                               Graphs$UndirectedEdge
                               IEdge
                               IGraph
                               IList
                               IMap
                               ISet
                               List
                               Set
                               )
           (java.util OptionalLong)
           (java.util.function BiPredicate
                               ToLongFunction)))

; Edges

(defn ^Graphs$DirectedEdge directed-edge
  "Constructs a new directed edge with an optional value."
  ([from to] (Graphs$DirectedEdge. nil from to))
  ([from to value] (Graphs$DirectedEdge. value from to)))

(defn ^Graphs$UndirectedEdge undirected-edge
  "Constructs a new undirected edge with an optional value."
  ([from to] (Graphs$UndirectedEdge. nil from to))
  ([from to value] (Graphs$UndirectedEdge. value from to)))

(defn edge-from
  "The source vertex of an edge"
  [^IEdge edge]
  (.from edge))

(defn edge-to
  "The destination vertex of an edge."
  [^IEdge edge]
  (.to edge))

(defn edge-value
  "The value associated with an edge."
  [^IEdge edge]
  (.value edge))

(defn edge-directed?
  "Is an edge from a directed graph?"
  [^IEdge edge]
  (.isDirected edge))

; Graphs

(defn ^Graph graph
  "Constructs a new Graph, optionally with custom hash and equality semantics
  (hash vertex) and (equals? vertex1 vertex2)."
  ([] (Graph.))
  ([hash-fn equals-fn]
   (Graph. (functional hash-fn) (functional equals-fn))))

(defn ^DirectedGraph digraph
  "Constructs a new directed graph, optionally with custom hash and equality
  semantics (hash vertex) and (equals? vertex1 vertex2)."
  ([] (DirectedGraph.))
  ([hash-fn equals-fn]
   (DirectedGraph. (functional hash-fn) (functional equals-fn))))

(defn ^DirectedGraph directed-acyclic-graph
  "Constructs a new directed acyclic graph, optionally with custom hash and
  equality semantics (hash vertex) and (equals? vertex1 vertex2)."
  ([] (DirectedAcyclicGraph.))
  ([hash-fn equals-fn]
   (DirectedAcyclicGraph. (functional hash-fn) (functional equals-fn))))

(defn ^IEdge edge
  "The edge value between `from` and `to`. Throws if edge doesn't exist."
  [^IGraph g, from, to]
  (.edge g from to))

(defn ^Iterable edges
  "An interable over every edge in the graph."
  [^IGraph g]
  (.edges g))

(defn ^IGraph add-edge
  "Adds an edge to the graph."
  [^IGraph g, ^IEdge edge]
  (.add g edge))

(defn ^IGraph add-vertex
  "Adds a vertex to the graph."
  [^IGraph g, x]
  ; Actually not sure how to do type hints here; there's a specific IEdge
  ; version of add, and another with a type parameter which is presumably
  ; erased...
  (.add g x))

(defn ^IGraph remove
  "Removes a vertex from the graph."
  [^IGraph g, x]
  (.remove g x))

(defn ^IGraph link
  "Returns graph with vertex `from` linked to `to`, optionally via edge `e`,
  optionally with `e` merged into an existing edge via `(merge extant-edge e)`."
  ([^IGraph g, from, to]
   (.link g from to))
  ([^IGraph g, from, to, e]
   (.link g from to e))
  ([^IGraph g, from, to, e, merge]
   (.link g from to e (functional merge))))

(defn ^IGraph unlink
  "Removes the link between `from` and `to`."
  [^IGraph g, from, to]
  (.unlink g from to))

(defn ^ISet vertices
  "The set of all vertices in the graph"
  [^IGraph g]
  (.vertices g))

(defn ^ISet in
  "The set of all vertices that lead directly into this vertex."
  [^IGraph g, v]
  (.in g v))

(defn ^ISet out
  "The set of all vertices this vertex directly leads to."
  [^IGraph g, v]
  (.out g v))

(defn map-edges
  "Transforms every edge's value by applying (f edge) -> value'"
  [^IGraph g, f]
  (.mapEdges g (functional f)))

(defn index-of
  "Returns the index of the given vertex, if present, or nil."
  [^IGraph g, v]
  (let [^OptionalLong i (.indexOf g v)]
    (when (.isPresent i)
      (.getAsLong i))))

(defn ^IGraph select
  "A graph containing only the specified vertices and the edges between them."
  [^IGraph g, ^ISet vertices]
  (.select g vertices))

(defn ^IGraph transpose
  "Transposes a graph, flipping the direction of each edge."
  [^IGraph g]
  (.transpose g))

(defn ^BiPredicate vertex-equality
  "Returns the vertex equality function of a graph"
  [^IGraph g]
  (.vertexEquality g))

(defn ^ToLongFunction vertex-hash
  "Returns the vertex hash function of a graph."
  [^IGraph g]
  (.vertexHash g))

(defn directed?
  "Is this graph directed?"
  [^IGraph g]
  (.isDirected g))

; DAGS

(defn ^Set top
  "This appears to be the vertices which have no in edges."
  [^DirectedAcyclicGraph g]
  (.top g))

(defn ^Set bottom
  "Unclear what this does. Might be broken?"
  [^DirectedAcyclicGraph g]
  (.bottom g))

; Graphs

(defn merge
  "Merges two graphs together using a function (merge-fn edge-value1
  edge-value2)."
  [^IGraph a, ^IGraph b, merge-fn]
  (Graphs/merge a b (functional merge-fn)))

(defn ^IList shortest-path
  "Finds the shortest path, if one exists, from starting vertex to any vertex
  where (accept? v) is truthy, using (cost edge) -> double to determine the
  cost of each edge. Nil if no path exists."
  [g, start, accept?, cost]
  (let [path (Graphs/shortestPath
               g start (functional accept?) (functional cost))]
    (.orElse path nil)))

(defn ^IList shortest-path-from-any
  "Finds the shortest path, if one exists, from any one of an iterable of
  starting vertices to any vertex where (accept? v) is truthy, using (cost
  edge) -> double to determine the cost of each edge. Nil if no path exists."
  [g, starts, accept?, cost]
  (let [path (Graphs/shortestPath
               g ^Iterable starts (functional accept?) (functional cost))]
    (.orElse path nil)))

(defn ^Set connected-components
  "Returns a set of sets of vertices where each vertex can reach every other
  vertex within the set."
  [^IGraph g]
  (Graphs/connectedComponents g))

(defn ^Set biconnected-components
  "Returns a set of sets of vertices where each vertex can reach every other
  vertex within the set, even if a single vertex is removed."
  [^IGraph g]
  (Graphs/biconnectedComponents g))

(defn ^Set articulation-points
  "Returns a set of vertices where the removal of that vertex will partition
  the graph."
  [g]
  (Graphs/articulationPoints g))

(defn ^Set strongly-connected-components
  "Sets of sets of vertices where each vertex can reach every other vertex in
  that set. include-singletons? indicates whether single-vertex sets are
  allowed."
  ([g]
   (Graphs/stronglyConnectedComponents g false))
  ([g include-singletons?]
    (Graphs/stronglyConnectedComponents g include-singletons?)))

(defn ^List strongly-connected-subgraphs
  "A list of graphs of strongly connected components in g. include-singletons?
  indicates whether single-vertex sets are allowed."
  ([g]
   (Graphs/stronglyConnectedSubgraphs g false))
  ([g include-singletons?]
    (Graphs/stronglyConnectedSubgraphs g include-singletons?)))

(defn ^List cycles
  "Returns a list of lists of all cyclical paths through the graph."
  [g]
  (Graphs/cycles g))

(defn ^Iterable bfs-vertices
  "Performs a breadth-first traversal through a graph implicitly defined by a
  function (adjacent v) -> [v1, v2, ...] (or any Iterable of vertices). Starts
  with a single vertex v, and returns an Iterable of vertices."
  [v adjacent]
  (Graphs/bfsVertices v (functional adjacent)))

(defn ^Iterable bfs-vertices-from-any
  "Like bfs-vertices, but takes an Iterable of starting vertices."
  [^Iterable vs adjacent]
  (Graphs/bfsVertices vs (functional adjacent)))
