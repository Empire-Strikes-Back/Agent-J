(ns Agent-J.main
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string]
   [clojure.pprint]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [Agent-J.seed :refer [root op]]
   [Agent-J.raisins]
   [Agent-J.peanuts]
   [Agent-J.kiwis]
   [Agent-J.salt]
   [Agent-J.carrots]
   [Agent-J.rolled-oats]))

(defmethod op :ping
  [value]
  (go
    (clojure.pprint/pprint value)
    (put! (:ui-send| root) {:op :pong
                            :from :program
                            :meatbuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (clojure.pprint/pprint value)))

(defmethod op :game
  [value]
  (go))

(defmethod op :leave
  [value]
  (go))

(defmethod op :discover
  [value]
  (go))

(defmethod op :settings
  [value]
  (go))

(defn ops-process
  [{:keys []
    :as opts}]
  (go
    (loop []
      (when-let [value (<! (:ops| root))]
        (<! (op value))
        (recur)))))

(defn -main []
  (println :main)

  (go
    (let []
      (println ":Agent-K do you what the most destructive force in the universe is?")
      (println ":_ sugar?")
      
      (<! (chan 1))

      (set! (.-defaultMaxListeners (.-EventEmitter (js/require "events"))) 100)
      (set! (.-AbortController js/global) (.-AbortController (js/require "node-abort-controller")))

      (<! (Agent-J.seed/process {}))

      (.ensureDirSync (js/require "fs-extra") (:program-data-dirpath root))

      (remove-watch (:stateA root) :watch-fn)
      (add-watch (:stateA root) :watch-fn
                 (fn [ref wathc-key old-state new-state]

                   (when (not= old-state new-state))))

      (ops-process {})

      (let [done| (chan 1)
            electron (js/require "electron")]
        (.on (.-app electron) "ready"
             (fn []
               (reset! (:windowA root) (electron.BrowserWindow. (clj->js {:width 1600
                                                                          :height 900
                                                                          :title ":_ hey, it is worth it? :K oh yeah, it's worth it"
                                                                          :icon (.join (js/require "path") js/__dirname "icon.png")
                                                                          :webPreferences {:nodeIntegration true
                                                                                           :contextIsolation false}})))
               (.loadURL ^js/electron.BrowserWindow @(:windowA root)  (str "file://" (.join (js/require "path") js/__dirname "ui" "index.html")))
               (.on ^js/electron.BrowserWindow @(:windowA root) "closed" #(reset! (:windowA root) nil))
               (.on (.-webContents @(:windowA root)) "did-finish-load"
                    (fn []
                      (put! (:ui-send| root) {:op :ping
                                              :if :you-re-seeing-things-running-through-your-head
                                              :who :ya-gonna-call?})))
               (close! done|)))
        (.on (.-app electron) "window-all-closed" (fn []
                                                    (when-not (= js/process.platform "darwin")
                                                      (.quit (.-app electron)))))
        (.on (.-app electron) "error" (fn [ex]
                                        (js/console.log ex)
                                        (close! done|)))
        (<! done|))

      (<! (Agent-J.rolled-oats/process {}))

      (let []
        (.on (.-ipcMain (js/require "electron")) "asynchronous-message"
             (fn [event message-string]
               (put! (:ops| root) (-> message-string #_(.toString) (read-string)))))
        (go
          (loop []
            (when-let [message (<! (:ui-send| root))]
              (.send (.-webContents @(:windowA root)) "asynchronous-message" (str message))
              (recur)))))

      (let [ipfs (.create (js/require "ipfs-http-client") "http://127.0.0.1:5001")
            orbitdb (<p!
                     (->
                      (.createInstance
                       (js/require "orbit-db") ipfs
                       (clj->js
                        {"directory" (:orbitdb-data-dirpath root)}))
                      (.catch (fn [ex]
                                (println ex)))))]
        (println (.. orbitdb -identity -id))))))


(comment

  (<p! (.create (js/require "ipfs")
                (clj->js
                 {:repo (.join (js/require "path") (.homedir (js/require "os")) ".Agent-J" "ipfs")})))

  ;
  )


(comment

  (go
    (loop []
      (when-let [{:keys [message from] :as value} (<! sub|)]
        (condp = (:op message)
          :game-state
          (let [{:keys [game-state]} message]
            (swap! gameA merge game-state))
          :player-state
          (let [{:keys [game-state]} message]
            (swap! gameA update-in [:players from] merge message))
          :games
          (let [{:keys [frequency host-peer-id]} message]
            (swap! gamesA update-in [frequency] merge message)))
        (recur))))

  (go
    (loop []
      (<! (timeout 3000))
      (let [expired (into []
                          (comp
                           (keep (fn [[frequency {:keys [timestamp]}]]
                                   #_(println (- (.getTime (java.util.Date.)) timestamp))
                                   (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                     frequency))))
                          @gamesA)]
        (when-not (empty? expired)
          (apply swap! gamesA dissoc expired)))
      (recur)))

  (go
    (loop []
      (<! (timeout 3000))
      (let [expired (into []
                          (comp
                           (keep (fn [[frequency {:keys [timestamp peer-id]}]]
                                   #_(println (- (.getTime (java.util.Date.)) timestamp))
                                   (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                     frequency))))
                          (:players @gameA))]
        (when-not (empty? expired)
          (apply swap! gameA update :players dissoc expired)))
      (recur)))

  (go
    (loop []
      (when-let [value (<! ops|)]
        (condp = (:op value)
          :game
          (let [{:keys [frequency role]} value
                id| (chan 1)
                port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
                ipfs-api-url (format "http://127.0.0.1:%s" port)
                games-topic (Agent-J.rolled-oats/encode-base64url-u "raisins")
                game-topic (Agent-J.rolled-oats/encode-base64url-u frequency)
                _ (Agent-J.rolled-oats/subscribe-process
                   {:sub| sub|
                    :cancel| cancel-sub|
                    :frequency frequency
                    :ipfs-api-url ipfs-api-url
                    :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
                    :id| id|})
                host? (= role :host)
                {:keys [peer-id]} (<! id|)]
            #_(println :game value)
            (go
              (loop []
                (alt!
                  cancel-pub|
                  ([_] (do nil))

                  (timeout 2000)
                  ([_]
                   (when host?
                     (Agent-J.rolled-oats/pubsub-pub
                      ipfs-api-url games-topic (str {:op :games
                                                     :timestamp (.getTime (java.util.Date.))
                                                     :frequency frequency
                                                     :host-peer-id peer-id}))
                     (Agent-J.rolled-oats/pubsub-pub
                      ipfs-api-url game-topic (str {:op :game-state
                                                    :timestamp (.getTime (java.util.Date.))
                                                    :game-state {:host-peer-id peer-id}})))

                   (Agent-J.rolled-oats/pubsub-pub
                    ipfs-api-url game-topic (str {:op :player-state
                                                  :timestamp (.getTime (java.util.Date.))
                                                  :peer-id peer-id}))
                   (recur))))))

          :leave
          (let [{:keys [frequency]} value]
            (>! cancel-sub| true)
            (>! cancel-pub| true)
            (reset! gameA {}))

          :discover
          (let [discover-jframe (JFrame. "discover")]
            (Agent-J.kiwis/discover-process
             {:jframe discover-jframe
              :root-jframe jframe
              :ops| ops|
              :gamesA gamesA
              :gameA gameA
              :stateA stateA})
            (reset! gameA @gameA))

          :settings
          (let [settings-jframe (JFrame. "settings")]
            (Agent-J.kiwis/settings-process
             {:jframe settings-jframe
              :root-jframe jframe
              :ops| ops|
              :settingsA settingsA})
            (reset! settingsA @settingsA))

          :settings-value
          (let []
            (swap! settingsA merge value))

          :host-yes
          (let [{:keys [frequency]} value]
            (println :frequency frequency)))

        (recur))))



  (let [port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
        ipfs-api-url (format "http://127.0.0.1:%s" port)
        id| (chan 1)
        raw-stream-connection-pool (Simba.http/connection-pool {:connection-options {:raw-stream? true}})]

    (alter-var-root #'raw-stream-connection-pool (constantly raw-stream-connection-pool))
    (Agent-J.rolled-oats/subscribe-process
     {:sub| sub|
      :raw-stream-connection-pool raw-stream-connection-pool
      :cancel| (chan (sliding-buffer 1))
      :frequency "raisins"
      :ipfs-api-url ipfs-api-url
      :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
      :id| id|}))

  ;
  )

(comment
  (.on js/global.process "beforeExit"
       (fn [code]
         (js/Promise. (fn [resolve reject]
                        (go
                          (<! (chan 1))
                          (resolve 0))))))

  (<! (chan 1))

  ;
  )