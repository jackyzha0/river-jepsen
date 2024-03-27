(ns jepsen.river
  (:require
   [clojure.tools.logging :refer :all]
   [jepsen.control.util :as cu]
   [jepsen.os.ubuntu :as ubuntu]
   [jepsen [cli :as cli]
    [control :as c]
    [db :as db]
    [tests :as tests]]))

(defn get-port
  "Returns the port number to use for the server"
  []
  (+ 42000 (rand-int 1000)))

(defn river-server
  "River server to test against"
  [cmd]
  (let [port (get-port)
        dir (cu/tmp-dir!)
        logfile (str dir "/server.log")
        pidfile (str dir "/server.pid")]
    (reify db/DB
      (setup! [_db _test node]
        (info node (str "starting river server on port " port))
        (cu/start-daemon!
         {:logfile logfile
          :pidfile pidfile
          :env {:PORT port}
          :chdir   dir}
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
          :db   (river-server "make bun-run")
          :ssh {:dummy? true}
          :pure-generators true}))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge
             (cli/single-test-cmd {:test-fn river-test})
             (cli/serve-cmd))
            args))
