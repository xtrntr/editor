(ns editor.components.canvas
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [editor.drawing :as draw]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn draw-image
  [app owner dom-node-ref]
  (let [offset-x (get-in @app [:canvas-offset-x])
        offset-y (get-in @app [:canvas-offset-y])
        zoom-factor (get-in @app [:zoom-factor])
        canvas (om/get-node owner dom-node-ref)
        width (.-width canvas)
        height (.-height canvas)
        ctx (.getContext canvas "2d")
        img (om/get-state owner :img)
        img-height (/ (.-height img) zoom-factor)
        img-width (/ (.-width img) zoom-factor)]
    (do
      (.clearRect ctx 0 0 width height)
      ;(.scale ctx zoom-factor zoom-factor)
      ;(.log js/console img-height)
      (.drawImage ctx img offset-x offset-y img-width img-height)
      )))

(defn background-canvas-component  
  [app owner]   
  (reify 
    om/IInitState
    (init-state [_]
      {:img (let [img (new js/Image)]
              (set! (.-src img) (get-in app [:background-image]))
              img)})

    om/IDidUpdate  
    (did-update [_ _ _] 
      (draw-image app owner "background-canvas-ref"))
    
    om/IDidMount
    (did-mount [_]
      (draw-image app owner "background-canvas-ref"))

    om/IRender
    (render [this]
      (let [width (get-in app [:canvas-width]) 
            height (get-in app [:canvas-height])]
        (dom/canvas
         #js {:id "background-canvas"
              :width width
              :height height
              :className "canvas"
              :ref "background-canvas-ref"})))))
