(ns editor.components.canvas
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [editor.drawing :as draw]
            [cljs.core.async :refer [put! chan <! alts!]]))

(def img (new js/Image))
(set! (.-src img) "img/circuitboard.jpg")

(defn draw-image
  [app owner dom-node-ref]
  (let [offset-x (get-in @app [:canvas-offset-x])
        offset-y (get-in @app [:canvas-offset-y])
        canvas (om/get-node owner dom-node-ref)
        width (.-width canvas)
        height (.-height canvas)
        ctx (.getContext canvas "2d")]
    (do
      (.clearRect ctx 0 0 width height)
      (.drawImage ctx img offset-x offset-y)
      )))


(defn background-canvas-component  
  [app owner]   
  (reify 
    om/IDidUpdate  
    (did-update [_ _ _] 
      (draw-image app owner "main-canvas-ref"))
    
    om/IDidMount
    (did-mount [_]
      (draw-image app owner "main-canvas-ref"))

    om/IRender
    (render [this]
      (let [width (get-in app [:canvas-width]) 
            height (get-in app [:canvas-height])]
        (dom/canvas
         #js {:id "main-canvas"
              :width width
              :height height
              :className "canvas"
              :ref "main-canvas-ref"})))))
