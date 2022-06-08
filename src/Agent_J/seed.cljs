(ns Agent-J.seed
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]))

(defmulti op :op)

(declare root)

(defn process
  [{:keys []
    :as opts}]
  (go
    (defonce root
      (let [program-data-dirpath (or
                                  (some->
                                   (.. js/global.process -env -Agent_J_PATH)
                                   (clojure.string/replace-first  #"~" (.homedir (js/require "os"))))
                                  (.join (js/require "path") (.homedir (js/require "os")) ".Agent-J"))]
        {:program-data-dirpath program-data-dirpath
         :state-file-filepath (.join (js/require "path") program-data-dirpath "Agent-J.edn")
         :orbitdb-data-dirpath (.join (js/require "path") program-data-dirpath "orbitdb")
         :port (or (try (.. js/global.process -env -PORT)
                        (catch js/Error ex nil))
                   3344)
         :stateA (atom nil)
         :windowA (atom nil)
         :host| (chan 1)
         :ops| (chan 10)
         :ui-send| (chan 10)}))))