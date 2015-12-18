(ns editor.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [cljsjs.fabric]))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))
(def mouse-chan (chan))

(def Canvas (js/React.createFactory js/fabric.Canvas))
(def Rect (js/React.createFactory js/fabric.Rec))

(defn debug []
  (.log js/console "maxD"))
                                        ;(.on canv "mouse:down" debug)

;; this will override fabric's events =(
(comment
  (def canvas (. js/document (getElementById "canv")))

  (defn listen [el type]
    (let [out (chan)]
      (events/listen el type
                     (fn [e] (put! out e)))
      out))

  (defn init []
    (let [clicks (listen canvas "click")]
      (go (while true
            (<! clicks)
            (.log js/console "xD")
            ))))
  (.log js/console canvas)
  (init))

(defn refresh-canvas [app-state owner]
  "all drawing functions to be included here"
  (let [canvas (om/get-node owner)
        context (.getContext canvas "2d")]
    (.log js/console "pressed something xD")
    ))

(defn main-canvas [app-state owner]
  (reify     
    om/IInitState
    (init-state [_]
      {:mouse-chan mouse-chan})
    om/IDidMount
    (did-mount [_]
      (let [window js/window
            canvas (. js/document (getElementById "canvas"))]
        (events/listen window "click" #(put! mouse-chan {:event % :mouseevent :click}))
        (events/listen canvas "mousedown" #(put! mouse-chan {:event % :mouseevent :mousedown}))
        (events/listen canvas "mouseup" #(put! mouse-chan {:event % :mouseevent :mouseup}))
        (events/listen canvas "mousemove" #(put! mouse-chan {:event % :mouseevent :mousemove})))
      (let [mouse-chan (om/get-state owner :mouse-chan)]
        (go (while true
              (let [mouse-event (<! mouse-chan)
                    mouse-keyword (:mouseevent mouse-event)]
                (cond
                 (= mouse-keyword :click) (.log js/console "xD")
                 (= mouse-keyword :mousemove) (.log js/console "move") 
                 (= mouse-keyword :mouseup) (.log js/console "mouseup")
                 (= mouse-keyword :mousedown) (.log js/console "mousedown")
                 :else (.log js/console "else")
                 ))))))
    om/IRender
    (render [_]
      (dom/canvas 
       #js {:id "canvas"
            :width 333
            :height 333})
      )))

(comment)
(om/root 
 main-canvas 
 app-state 
 {:target (. js/document (getElementById "canv-wrapper"))})

(comment)
(def canv (new js/fabric.Canvas "canvas"))
(def rect (new js/fabric.Rect #js {:left 20
                                   :top 20
                                   :fill "red"
                                   :width 20
                                   :height 20}))
(def cat-element (. js/document (getElementById "img")))
(def cat-instance (new js/fabric.Image cat-element #js {:left 0
                                                        :top 0
                                                        :opacity 0.85
                                                        }))
;(.add canv cat-instance)
(.add canv rect)
(.setBackgroundImage canv cat-instance (.bind (.-renderAll canv) canv) 
                     #js {:originX "left"
                          :originY "top"})

(comment)
(.log js/console canv)
(.log js/console (. js/document (getElementById "img")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
