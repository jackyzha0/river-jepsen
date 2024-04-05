(ns jepsen.river
  (:require
   [clojure.tools.logging :refer [info]]
   [jepsen [cli :as cli]
    [control :as c]
    [db :as db]
    [tests :as tests]]
   [jepsen.control.util :as cu]))

(defn get-port
  "Returns the port number to use for the server"
  []
  (+ 42000 (rand-int 1000)))

(def root "/root")
(def dir "/tmp/jepsen")
(def logfile (str dir "/server.log"))
(def pidfile (str dir "/server.pid"))
(defn river-server
  "River server to test against"
  [cmd]
  (let [port (get-port)]
    (reify db/DB
      (setup! [_db _test node]
        ; setup tmp dir
        (c/exec :mkdir :-p dir)

        ; start
        (info node (str "starting river server on port " port))
        (cu/start-daemon!
         {:logfile logfile
          :pidfile pidfile
          :env {:PORT port}
          :chdir   root}
         cmd)
        (cu/await-tcp-port port))

      (teardown! [_db _test node]
        (info node "stopping river server")
        (cu/stop-daemon! cmd pidfile)
        (c/exec :rm :-rf dir))

      db/LogFiles
      (log-files [_ _test _node]
        [logfile]))))

(defn river-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  (merge tests/noop-test
         opts
         {:name "basic river"
          :db   (river-server "bun-server")
          :ssh {:private-key-path "/root/.ssh/id_rsa"}
          :pure-generators true}))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge
             (cli/single-test-cmd {:test-fn river-test})
             (cli/serve-cmd))
            args))

