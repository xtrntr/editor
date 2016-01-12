(ns editor.components.canvas
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [editor.drawing :as draw]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn pixel2canvas
  [point offset zoom]
  (+ offset (/ point zoom)))

(defn abs [n] (max n (- n)))

(defn draw-image
  [app owner dom-node-ref]
  (let [canvas-offset-x (get-in @app [:drawing :canvas-offset-x])
        canvas-offset-y (get-in @app [:drawing :canvas-offset-y])
        zoom (get-in @app [:drawing :zoom-factor])
        pixel-x1 (get-in @app [:drawing :x1])
        pixel-y1 (get-in @app [:drawing :y1])
        pixel-x2 (get-in @app [:drawing :x2])
        pixel-y2 (get-in @app [:drawing :y2])
        canvas-x1 (pixel2canvas pixel-x1 canvas-offset-x zoom)
        canvas-y1 (pixel2canvas pixel-y1 canvas-offset-y zoom)
        canvas-x2 (pixel2canvas pixel-x2 canvas-offset-x zoom)
        canvas-y2 (pixel2canvas pixel-y2 canvas-offset-y zoom)
        canvas (om/get-node owner dom-node-ref)
        width (.-width canvas)
        height (.-height canvas)
        ctx (.getContext canvas "2d")
        img (om/get-state owner :img)
        img-height (/ (.-height img) zoom)
        img-width (/ (.-width img) zoom)]
    (do
      (.clearRect ctx 0 0 width height)
      (.drawImage ctx img canvas-offset-x canvas-offset-y img-width img-height)
      )))

(defn draw-selected-image
  [app owner dom-node-ref]
  (let [canvas (om/get-node owner dom-node-ref)
        width (.-width canvas)
        height (.-height canvas)
        ctx (.getContext canvas "2d")
        img (om/get-state owner :img)]
    (do
      (.clearRect ctx 0 0 width height)
      (.drawImage ctx img 0 0)
      )))

(defn save-image
  [app owner dom-node-ref]
  (let [canvas-offset-x (get-in @app [:drawing :canvas-offset-x])
        canvas-offset-y (get-in @app [:drawing :canvas-offset-y])
        zoom (get-in @app [:drawing :zoom-factor])
        pixel-x1 (get-in @app [:drawing :x1])
        pixel-y1 (get-in @app [:drawing :y1])
        pixel-x2 (get-in @app [:drawing :x2])
        pixel-y2 (get-in @app [:drawing :y2])
        canvas-x1 (pixel2canvas pixel-x1 canvas-offset-x zoom)
        canvas-y1 (pixel2canvas pixel-y1 canvas-offset-y zoom)
        canvas-x2 (pixel2canvas pixel-x2 canvas-offset-x zoom)
        canvas-y2 (pixel2canvas pixel-y2 canvas-offset-y zoom)
        canvas (om/get-node owner dom-node-ref)
        img-width (abs (- pixel-y1 pixel-y2))
        img-height (abs (- pixel-x1 pixel-x2))
        ctx (.getContext canvas "2d")
        element-draw-step (get-in @app [:drawing :element-draw-step])
        step3 (= element-draw-step 3)
        element-to-draw (get-in @app [:drawing :element-to-draw])
        img-being-selected (= element-to-draw :select-img)]
    (when (and step3 img-being-selected)
      (om/update! app [:main-app :selected-image :img] 
                  (.getImageData ctx canvas-x1 canvas-y1 img-width img-height))
      (om/update! app [:drawing :element-draw-step] 1)
      ))) 

(defn imgdisplay-canvas-component
  [app owner]
  (reify 
    om/IInitState
    (init-state [_]
      {:img (let [img (new js/Image)]
              img)})

    om/IDidUpdate  
    (did-update [_ _ _] 
      (draw-selected-image app owner "imgdisplay-canvas-ref"))
    
    om/IDidMount
    (did-mount [_]
      (draw-selected-image app owner "imgdisplay-canvas-ref"))

    om/IRender
    (render [this]
      (let [width (get-in @app [:main-app :selected-image :width]) 
            height (get-in @app [:main-app :selected-image :height])]
        (dom/canvas
         #js {:id "imgdisplay-canvas"
              :width width
              :height height
              :className "imgdisplay-canvas"
              :ref "imgdisplay-canvas-ref"})))))

(defn background-canvas-component  
  [app owner]   
  (reify 
    om/IInitState
    (init-state [_]
      {:img (let [img (new js/Image)]
              (set! (.-src img) (get-in app [:main-app :background-image]))
              img)})

    om/IDidUpdate  
    (did-update [_ _ _] 
      (draw-image app owner "background-canvas-ref")
      (save-image app owner "background-canvas-ref"))
    
    om/IDidMount
    (did-mount [_]
      (draw-image app owner "background-canvas-ref")
      (save-image app owner "background-canvas-ref"))

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
