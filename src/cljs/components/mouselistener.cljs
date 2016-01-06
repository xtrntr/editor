(ns editor.components.mouselistener
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]))

;this function takes a point (x or y) on the canvas , canvas offset and zoom factor then determines where the point (x or y) is in pixels/picture coordinates 
(defn canvas2pixel
  [point offset zoom]
  (* zoom (- point offset)))

(defn abs [n] (max n (- n)))

(defn handle-mouse-event [app owner e]
  (let [event-type (.-type e)
        mouse-down? (= event-type "mousedown")
        mouse-up? (= event-type "mouseup")
        mouse-move? (= event-type "mousemove")
        mouse-wheel? (= event-type "mousewheel")
        command-mode (get-in @app [:tools :command-mode])
        panning-mode (= :panning command-mode)
        drawing-mode (= :drawing command-mode)

        ;variables for panning
        last-off-x (om/get-state owner :before-panning-offset-x)
        last-off-y (om/get-state owner :before-panning-offset-y)
        point-of-click-x (om/get-state owner :point-of-click-x)
        point-of-click-y (om/get-state owner :point-of-click-y)
        canvas-x (.-offsetX e)
        canvas-y (.-offsetY e)
        canvas-offset-x (get-in @app [:canvas-offset-x])
        canvas-offset-y (get-in @app [:canvas-offset-y])
        zoom (get-in @app [:zoom-factor])
        pixel-x (canvas2pixel canvas-x canvas-offset-x zoom)
        pixel-y (canvas2pixel canvas-y canvas-offset-y zoom)

        ;variables for drawing
        element-draw-step (get-in @app [:drawing :element-draw-step])
        step1 (= element-draw-step 1)
        step2 (= element-draw-step 2)
        step3 (= element-draw-step 3)
        element-to-draw (get-in @app [:drawing :element-to-draw])
        rect-being-drawn (= element-to-draw :rectangle)
        circ-being-drawn (= element-to-draw :circle)
        polyline-being-drawn (= element-to-draw :polyline)
        arc-being-drawn (= element-to-draw :arc)
        dot-being-drawn (= element-to-draw :dot)
        line-being-drawn (= element-to-draw :line)
        paint-color (get-in @app [:tools :paint-color])
        
        ;variables for zooming
        ;a mouse wheel will be +/- 120
        wheel-up? (= (.-wheelDelta (.-event_ e)) 120)
        wheel-down? (= (- 120) (.-wheelDelta (.-event_ e)))
        
        ;conditions
        start-panning? (and mouse-down? panning-mode)
        is-panning? (and mouse-move? panning-mode (om/get-state owner :user-is-panning))
        was-panning? (and panning-mode (om/get-state owner :user-is-panning))
        zoom-out? (and wheel-up? panning-mode)
        zoom-in? (and wheel-down? panning-mode)
        stop-panning? (and mouse-up? was-panning?)
        start-drawing? (and drawing-mode mouse-down?)
        is-drawing? (and drawing-mode (not dot-being-drawn))
        ]
    
    (om/update! app [:canvas-y] canvas-y)
    (om/update! app [:canvas-x] canvas-x)
    
 
    (when start-drawing?
      (when dot-being-drawn
        (om/transact! app [:elements] 
          (fn [x] (conj x {:type :dot :x pixel-x :y pixel-y}))))
      (when line-being-drawn
        (when step1
          (om/update! app [:drawing :x1] pixel-x)
          (om/update! app [:drawing :y1] pixel-y)
          (om/update! app [:drawing :x2] pixel-x)
          (om/update! app [:drawing :y2] pixel-y)
          (om/update! app [:drawing :element-draw-step] 2)
          )
        (when step2
          (let [x1 (get-in @app [:drawing :x1])
                y1 (get-in @app [:drawing :y1])
                delta-x (abs (- x1 pixel-x))
                delta-y (abs (- y1 pixel-y))
                snap-to-x-axis (> delta-x delta-y)]
            (om/transact! app [:elements]
              (fn [x] 
                (if snap-to-x-axis
                  (conj x {:type :line :x1 x1 :y1 y1 :x2 pixel-x :y2 y1
                           :color paint-color})
                  (conj x {:type :line :x1 x1 :y1 y1 :x2 x1 :y2 pixel-y
                           :color paint-color})))))
          (om/update! app [:drawing :element-draw-step] 1)
          )))
    (when is-drawing?
      (when step1
        (om/update! app [:drawing :x1] pixel-x)
        (om/update! app [:drawing :y1] pixel-y))
      (when step2
        (let [x1 (get-in @app [:drawing :x1])
              y1 (get-in @app [:drawing :y1])
              delta-x (abs (- x1 pixel-x))
              delta-y (abs (- y1 pixel-y))
              snap-to-x-axis (> delta-x delta-y)]
          (if snap-to-x-axis
            (do (om/update! app [:drawing :x2] pixel-x)
                (om/update! app [:drawing :y2] y1))
            (do (om/update! app [:drawing :y2] pixel-y)
                (om/update! app [:drawing :x2] x1)))))
      (when step3
        (om/update! app [:drawing :x3] pixel-x)
        (om/update! app [:drawing :y3] pixel-y)))
    (when start-panning?
      (om/set-state! owner :user-is-panning true)
      (om/set-state! owner :point-of-click-x canvas-x)
      (om/set-state! owner :point-of-click-y canvas-y)
      (om/set-state! owner :before-panning-offset-x canvas-offset-x)
      (om/set-state! owner :before-panning-offset-y canvas-offset-y)) 
    (when stop-panning?
      (om/set-state! owner :user-is-panning false)) 
    (when is-panning?
      (om/update! app [:canvas-offset-x] (+ last-off-x (- canvas-x point-of-click-x)))
      (om/update! app [:canvas-offset-y] (+ last-off-y (- canvas-y point-of-click-y))))
    (when zoom-out?
      (let [new-zoom (+ zoom 0.1) 
            new-canvas-offset-x (- (/ (- pixel-x (* canvas-x new-zoom)) new-zoom))
            new-canvas-offset-y (- (/ (- pixel-y (* canvas-y new-zoom)) new-zoom))]
        (om/update! app [:zoom-factor]  new-zoom)
        (om/update! app [:canvas-offset-x] new-canvas-offset-x)
        (om/update! app [:canvas-offset-y] new-canvas-offset-y)))
    (when zoom-in?
      (let [new-zoom (- zoom 0.1)
            new-canvas-offset-x (- (/ (- pixel-x (* canvas-x new-zoom)) new-zoom))
            new-canvas-offset-y (- (/ (- pixel-y (* canvas-y new-zoom)) new-zoom))]
        (om/update! app [:zoom-factor]  new-zoom)
        (om/update! app [:canvas-offset-x] new-canvas-offset-x)
        (om/update! app [:canvas-offset-y] new-canvas-offset-y)))
    ))

(defn mouse-listener-component
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:mouse-chan (chan)
       :is-mouse-panning false
       :before-panning-offset-x 0
       :before-panning-offset-y 0
       :point-of-click-x 0
       :point-of-click-y 0})
    
    ;go loop that handles event
    om/IWillMount
    (will-mount [_]
      (let [mouse-chan (om/get-state owner :mouse-chan)] 
        (go
          (loop []
            (let [[v ch] (alts! [mouse-chan])]
              (when (= ch mouse-chan) (handle-mouse-event app owner v))
              (recur))))))

    ;attach event listeners here.
    om/IDidMount
    (did-mount [_]
      (let [painter-watcher (om/get-node owner "painter-watcher-ref")
            mouse-chan (om/get-state owner :mouse-chan)]
        (events/listen painter-watcher "mousemove" #(put! mouse-chan %))
        (events/listen painter-watcher "mousedown" #(put! mouse-chan %))
        (events/listen painter-watcher "mouseup" #(put! mouse-chan %))
        (events/listen painter-watcher "mousewheel" #(put! mouse-chan %))))
    
    om/IRender
    (render [this]
      (let [screen-canvas-width (get-in app [:canvas-width])
            screen-canvas-height (get-in app [:canvas-height])
            zoom-factor (get-in app [:zoom-factor])]
        (dom/div #js {:id "painter-watcher"
                      :style #js {:width screen-canvas-width
                                  :height screen-canvas-height}
                      :ref "painter-watcher-ref"})))))
