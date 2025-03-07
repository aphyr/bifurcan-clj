(defproject com.aphyr/bifurcan-clj "0.1.4-SNAPSHOT"
  :description "Clojure wrappers for the Bifurcan library of high-performance data structures"
  :url "https://github.com/aphyr/bifurcan-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[io.lacuna/bifurcan "0.2.0-alpha7"]]
  :repl-options {:init-ns bifurcan-clj.core}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.12.0"]]}}
  :test-selectors {:buggy :buggy
                   :default (fn [m]
                              (not (or (:buggy m))))})
