(defproject jepsen "0.1.0-SNAPSHOT"
  :description "A Jepsen test for @replit/river"
  :main jepsen.river
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [jepsen "0.3.5"]
                 [verschlimmbesserung "0.1.3"]])
