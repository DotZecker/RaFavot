(defproject rafavot "0.0.1"
  :description "Rafa Twitter bot"
  :url "http://rafa.im/rafavot"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [jarohen/chime "0.1.9"]
                 [twitter-api "0.7.8"]]
  :main ^:skip-aot rafavot.core
  :target-path "target/%s"
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot :all}})
