(defproject harness "0.1.0-SNAPSHOT"
  :description "A Jepsen test for @replit/river"
  :main harness.driver
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [me.raynes/conch "0.8.0"]
                 [jepsen "0.3.5"]])
