(defproject org.bodil/bodol "0.1.0"
  :description "The BODil Oriented Language (working title)"
  :url "https://github.com/bodil/bodol"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [instaparse "1.4.10"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/core.logic "1.0.0"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :main bodol.repl
  :profiles
  {:uberjar {:aot :all
             :main bodol.repl.REPL}})
