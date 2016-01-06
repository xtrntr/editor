(ns editor.components.overlaycanvas
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [editor.drawing :as draw]
            [editor.drawing_utilities :refer [draw-line draw-dot]]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn canvas2pixel
  [point offset zoom]
  (* zoom (- point offset)))

(defn pixel2canvas
  [point offset zoom]
  (+ offset (/ point zoom)))

(defn draw-overlay-active
  [app owner dom-node-ref]
  (let [canvas (om/get-node owner dom-node-ref)
        ctx (.getContext canvas "2d") 
        width (.-width canvas)
        height (.-height canvas)

        paint-color (get-in @app [:tools :paint-color])
        element-draw-step (get-in @app [:drawing :element-draw-step])
        step1 (= element-draw-step 1)
        step2 (= element-draw-step 2)
        step3 (= element-draw-step 3)

        element-to-draw (get-in @app [:drawing :element-to-draw])
        canvas-offset-x (get-in @app [:canvas-offset-x])
        canvas-offset-y (get-in @app [:canvas-offset-y])
        zoom (get-in @app [:zoom-factor])
        canvas-x1 (pixel2canvas (get-in @app [:drawing :x1]) canvas-offset-x zoom)
        canvas-y1 (pixel2canvas (get-in @app [:drawing :y1]) canvas-offset-y zoom)
        canvas-x2 (pixel2canvas (get-in @app [:drawing :x2]) canvas-offset-x zoom)
        canvas-y2 (pixel2canvas (get-in @app [:drawing :y2]) canvas-offset-y zoom)
        canvas-x3 (pixel2canvas (get-in @app [:drawing :x3]) canvas-offset-x zoom)
        canvas-y3 (pixel2canvas (get-in @app [:drawing :y3]) canvas-offset-y zoom)

        ;conditions
        rect-being-drawn (= element-to-draw :rectangle)
        circ-being-drawn (= element-to-draw :circle)
        polyline-being-drawn (= element-to-draw :polyline)
        arc-being-drawn (= element-to-draw :arc)
        dot-being-drawn (= element-to-draw :dot)
        line-being-drawn (= element-to-draw :line)
        ]
    
    (when (and step1 line-being-drawn)
      (.clearRect ctx 0 0 width height))
    (when (and step2 line-being-drawn)
      (.clearRect ctx 0 0 width height) 
      (draw-line ctx canvas-x1 canvas-y1 canvas-x2 canvas-y2 paint-color))
    ))

(defn draw-overlay-drawn
  [app owner dom-node-ref]
  (let [canvas (om/get-node owner dom-node-ref)
        ctx (.getContext canvas "2d") 
        width (.-width canvas)
        height (.-height canvas)
        elements (get-in @app [:elements])
        canvas-offset-x (get-in @app [:canvas-offset-x])
        canvas-offset-y (get-in @app [:canvas-offset-y])
        zoom (get-in @app [:zoom-factor])
        ]
    (doseq [element elements]
      (let [{type :type} element]
        (when (= :line type)
          (let [conv-x (fn [x] (pixel2canvas x canvas-offset-x zoom))
                conv-y (fn [y] (pixel2canvas y canvas-offset-y zoom))
                {:keys [x1 y1 x2 y2 color]} element]
            (draw-line ctx (conv-x x1) (conv-y y1) (conv-x x2) (conv-y y2) color))
          )))
    
    ))

(defn overlay-canvas-component  
  [app owner]   
  (reify
    om/IDidUpdate
    (did-update [_ _ _]
      (draw-overlay-active app owner "overlay-canvas-ref")
      (draw-overlay-drawn app owner "overlay-canvas-ref"))
    
    om/IDidMount
    (did-mount [_]
      (draw-overlay-active app owner "overlay-canvas-ref")
      (draw-overlay-drawn app owner "overlay-canvas-ref"))
    
    om/IRender
    (render [this]
      (let [width (get-in app [:canvas-width]) 
            height (get-in app [:canvas-height])]
        (dom/canvas
         #js {:id "overlay-canvas"
              :width width
              :height height
              :className "canvas"
              :ref "overlay-canvas-ref"})))))
