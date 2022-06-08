(ns Agent-J.ui-corn
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
   [goog.events]

   ["react" :as react]
   ["react-dom/client" :as react-dom.client]

   [reagent.core]
   [reagent.dom]

   ["antd/lib/layout" :default AntdLayout]
   ["antd/lib/menu" :default AntdMenu]
   ["antd/lib/button" :default AntdButton]
   ["antd/lib/row" :default AntdRow]
   ["antd/lib/col" :default AntdCol]
   ["antd/lib/input" :default AntdInput]
   ["antd/lib/table" :default AntdTable]


   [clojure.test.check.generators]
   [clojure.spec.alpha :as s]

   [Agent-J.ui-seed :refer [root op]]))

(defmethod op :create-game
  [value]
  (println value))

(defn rc-tab
  []
  (reagent.core/with-let
    [dataA (reagent.core/cursor (:stateA root) [:simple-double-full])]
    [:> (.-Content AntdLayout)
     {:style {:background-color "white"}}
     [:> AntdRow
      [:> AntdTable
       {:size "small"
        :style {:width "100%"
                :height "80%"}
        :columns [{:title "id"
                   :dataIndex "id"
                   :key "name"}
                  {:title "name"
                   :dataIndex "name"
                   :key "name"}]
        :dataSource []}]]]))

(defn process
  [{:keys []
    :as opts}]
  (let []))