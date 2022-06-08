(ns Agent-J.ui-seed
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

   ["react-dom/client" :as react-dom.client]
   [reagent.core]))

(defmulti op :op)

(defonce root (let []
                {:matchA (reagent.core/atom nil)
                 :stateA (reagent.core/atom {})
                 :ops| (chan (sliding-buffer 10))
                 :program-send| (chan 10)
                 :dom-rootA (atom (react-dom.client/createRoot (.getElementById js/document "ui")))}))