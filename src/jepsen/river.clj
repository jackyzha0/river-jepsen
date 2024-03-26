(ns jepsen.river
  (:require [jepsen.cli :as cli]
            [jepsen.tests :as tests]))


(defn river-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  (merge tests/noop-test
         { :pure-generators true }
         (cli/merge-opt-specs opts { :ssh {:dummy? true} })))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (cli/single-test-cmd {:test-fn river-test})
            args))