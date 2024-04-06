(ns harness.driver
  (:require
   [clojure.tools.logging :refer [info]]
   [jepsen [cli :as cli]
    [tests :as tests]]
   [jepsen.client :as client]
   [jepsen.control :as c]
   [jepsen.control.util :as cu]
   [jepsen.db :as db]
   [jepsen.generator :as generator]
   [jepsen.os.debian :as debian]
   [me.raynes.conch.low-level :as sh]))

(def root "/jepsen/src/fixtures")
(def dir "/tmp/jepsen")
(def serverlogfile (str dir "/server.log"))
(def serverpidfile (str dir "/server.pid"))
(defn get-port
  "Returns the port number to use for the server"
  []
  (+ 42000 (rand-int 1000)))

; ops
(defn rand-id [] (apply str (repeatedly 6 #(rand-nth "abcdefghijklmnopqrstuvwxyz0123456789"))))
(defn kv-set   [_ _] {:type :invoke, :f :kv-set, :value nil})

(defrecord Client
           [cmd port conn proc]
  client/Client
  (open! [this _test _node]
    (info (str "starting client" cmd))
    (let
     [process (sh/proc cmd :dir root)]
      (assoc this :proc process)))

  (setup! [this test])

  (invoke! [_ test op]
    (case (:f op)
      :kv-set (let [id (rand-id)
                    k "foo"
                    v (rand-int 100)]
                (do
                  (sh/feed-from-string proc (format "%s -- kv set -> %s %d\n" id k v))
                  (sh/read-line proc :out)))))

  (teardown! [this test])

  (close! [_ test]))

(defn river-server
  "River server to test against"
  [cmd port]
  (reify db/DB
    (setup! [_db _test node]
      ; setup tmp dir
      (c/exec :mkdir :-p dir)

        ; start
      (info node (str "starting river server" cmd))
      (cu/start-daemon!
       {:logfile serverlogfile
        :pidfile serverpidfile
        :env {:PORT port}
        :chdir   root}
       cmd)
      (cu/await-tcp-port port))

    (teardown! [_db _test node]
      (info node "stopping river server")
      (cu/stop-daemon! cmd serverpidfile)
      (c/exec :rm :-rf dir))

    db/LogFiles
    (log-files [_ _test _node]
      [serverlogfile])))

(defn river-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map."
  [opts]
  (merge tests/noop-test
         opts
         (let [port (get-port)]
           {:name "basic_river"
            :os   debian/os
            :db   (river-server "./typescript/server.sh" port)
            :client (Client. "./typescript/client.sh" port nil nil)
            :ssh  {:private-key-path "/root/.ssh/id_rsa"}
            :pure-generators true
            :generate (->> kv-set
                           (generator/stagger 1)
                           (generator/time-limit 10))})))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (cli/run! (merge
             (cli/single-test-cmd {:test-fn river-test})
             (cli/serve-cmd))
            args))

