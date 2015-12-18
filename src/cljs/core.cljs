(ns editor.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [cljsjs.fabric]))

;; define your app data so that it doesn't get over-written on reload

(def app-state 
  (atom 
   {:panning false
    :text "kay"}))

(def mouse-chan (chan))

(defn main-canvas [app-state owner]
  (reify     
    om/IInitState
    (init-state [_] 
      {:mouse-chan mouse-chan})
    om/IDidMount
    (did-mount [_]
      (let [window js/window
            canvas (. js/document (getElementById "canv-wrapper"))]
        (events/listen canvas "click" #(put! mouse-chan {:event % :mouseevent :click}))
        (events/listen canvas "mousedown" #(put! mouse-chan {:event % :mouseevent :mousedown}))
        (events/listen canvas "mouseup" #(put! mouse-chan {:event % :mouseevent :mouseup}))
        (events/listen canvas "mousemove" #(put! mouse-chan {:event % :mouseevent :mousemove})))
      (let [mouse-chan (om/get-state owner :mouse-chan)]
        (go (while true
              (let [mouse-event (<! mouse-chan)
                    mouse-keyword (:mouseevent mouse-event)]
                (cond
                 (= mouse-keyword :click) (do (om/set-state! owner [:panning] true)
                                              )
                 (= mouse-keyword :mousemove) (if (= true (om/get-state owner [:panning]))
                                                (.log js/console "pan")
                                                (.log js/console "notpan"))
                 (= mouse-keyword :mouseup) (om/set-state! owner [:panning] false)
                 (= mouse-keyword :mousedown) (.log js/console (om/get-state owner [:panning]))
                 :else (.log js/console "else")
                 ))))))
    om/IRender
    (render [_]
      (dom/canvas 
       #js {:id "canvas"
            :width 333
            :height 333})
      )))

(om/root 
 main-canvas 
 app-state 
 {:target (. js/document (getElementById "canv-wrapper"))})

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
