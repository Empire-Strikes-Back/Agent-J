(ns Agent-J.ui-main
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

   [reagent.core :as reagent.core]
   [reagent.dom :as reagent.dom]

   ["antd/lib/layout" :default AntdLayout]
   ["antd/lib/menu" :default AntdMenu]
   ["antd/lib/button" :default AntdButton]
   ["antd/lib/row" :default AntdRow]
   ["antd/lib/col" :default AntdCol]
   ["antd/lib/input" :default AntdInput]
   ["antd/lib/table" :default AntdTable]
   ["antd/lib/tabs" :default AntdTabs]

   [clojure.test.check.generators]
   [clojure.spec.alpha :as s]

   ["konva/lib/shapes/Rect"]
   ["konva" :default Konva]
   ["react-konva" :as ReactKonva :rename {Stage KonvaStage
                                          Layer KonvaLayer
                                          Rect KonvaRect
                                          Path KonvaPath
                                          Circle KonvaCircle
                                          Group KonvaGroup
                                          Wedge KonvaWedge
                                          RegularPolygon KonvaRegularPolygon}]

   ["@react-spring/web" :as ReactSpring :rename {animated ReactSpringAnimated
                                                 Spring ReactSpringSpring}]

   [Agent-J.ui-seed :refer [root]]
   [Agent-J.ui-corn]
   #_[Agent-J.Ritchi]))

(when (exists? js/module)
  (let [ipcRenderer (or (.-ipcRenderer (js/require "electron"))
                        (aget (js/require "electron") "ipcRenderer"))]
    (put! (:program-send| root) {:op :ping
                                 :from :ui
                                 :if :there-is-sompn-strage-in-your-neighbourhood
                                 :who :ya-gonna-call?})

    (.on ipcRenderer "asynchronous-message" (fn [event message-string]
                                              (put! (:ops| root) (read-string message-string))))
    (go
      (loop []
        (when-let [value (<! (:program-send| root))]
          (.send ipcRenderer "asynchronous-message" (str value))
          (recur))))))

(def colors
  {:sands "#edd3af" #_"#D2B48Cff"
   :Korvus "lightgrey"
   :signal-tower "brown"
   :recharge "#30ad23"
   :Agent-J "blue"})

(def ^:const box-size 32)
(def ^:const rows 16)
(def ^:const cols 16)

(defn rc-background-layer
  []
  (reagent.core/with-let
    []
    [:> (.-Layer ReactKonva)
     {:id "background-layer"}
     [:> (.-Rect ReactKonva) {:width (* box-size cols)
                              :height (* box-size rows)
                              :id "background-rect"
                              :x 0
                              :y 0
                              :fill "#ffffff"
                              :strokeWidth 0
                              :stroke "white"}]]))

(defn rc-game
  [match stateA ops|]
  (reagent.core/with-let
    []
    [:> (.-Stage ReactKonva)
     {:width (* box-size cols)
      :height (* box-size rows)}
     [rc-background-layer]]))

(defn canvas-process
  [{:keys []
    :as opts}]
  (let []))

(defn rc-main-tab
  [match stateA ops|]
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:div {}
    [:div "well, maybe my report is gonna reflect some shit too, K"]]])

(defn rc-game-tab
  [match stateA ops|]
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdRow
    [:> AntdCol
     {:flex 1}
     [rc-game match stateA ops|]]]])

(defn rc-settings-tab
  [match stateA ops|]
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdRow
    "settings"
    #_(str "settings" (:rand-int @stateA))]])

(defn websocket-process
  [{:keys [send| recv|]
    :as opts}]
  (let [socket (js/WebSocket. "ws://localhost:3344/ws")]
    (.addEventListener socket "open" (fn [event]
                                       (println :websocket-open)
                                       (put! send| {:op :ping
                                                    :from :ui
                                                    :if :there-is-sompn-strage-in-your-neighbourhood
                                                    :who :ya-gonna-call?})))
    (.addEventListener socket "message" (fn [event]
                                          (put! recv| (read-string (.-data event)))))
    (.addEventListener socket "close" (fn [event]
                                        (println :websocket-close event)))
    (.addEventListener socket "error" (fn [event]
                                        (println :websocket-error event)))
    (go
      (loop []
        (when-let [value (<! send|)]
          (.send socket (str value))
          (recur))))))

(defn rc-ui
  []
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdTabs
    {:size "small"}
    [:>  (.-TabPane AntdTabs)
     {:tab "discover" :key :rc-discover-tab}
     [Agent-J.ui-corn/rc-tab]]
    [:>  (.-TabPane AntdTabs)
     {:tab "game" :key :rc-game-tab}
     [rc-game-tab]]]])

(defmulti op :op)

(defmethod op :ping
  [value]
  (go
    (clojure.pprint/pprint value)
    (put! (:program-send| root) {:op :pong
                                 :from :ui
                                 :moneybuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (clojure.pprint/pprint value)))

(defn ops-process
  [{:keys []
    :as opts}]
  (go
    (loop []
      (when-let [value (<! (:ops| root))]
        (<! (op value))
        (recur)))))

(defn -main
  []
  (go
    #_(<! (timeout 1000))
    (println "twelve is the new twony")
    (println ":Madison you though i was a zombie?")
    (println ":Columbus yeah, of course - a zombie")
    (println ":Madison oh my God, no - i dont even eat meat - i'm a vegatarian - vegan actually")
    #_(set! (.-innerHTML (.getElementById js/document "ui"))
            ":Co-Pilot i saw your planet destroyed - i was on the Death Star :_ which one?")
    (ops-process {})
    (.render @(:dom-rootA root) (reagent.core/as-element [rc-ui]))
    #_(websocket-process {:send| (:program-send| root)
                        :recv| (:ops| root)})))

(defn reload
  []
  (when-let [dom-root @(:dom-rootA root)]
    (.unmount dom-root)
    (let [new-dom-root (react-dom.client/createRoot (.getElementById js/document "ui"))]
      (reset! (:dom-rootA root) new-dom-root)
      (.render @(:dom-rootA root) (reagent.core/as-element [rc-ui])))))

#_(-main)