(ns editor.components.mouselistener
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn handle-command
  [app owner event])

(defn abs [n] (max n (- n)))

(defn handle-mouse-event [app owner e]
  (let [event-type (.-type e)
        mouse-down? (= event-type "mousedown")
        mouse-up? (= event-type "mouseup")
        mouse-move? (= event-type "mousemove")
        command-mode (get-in @app [:tools :command-type])
        panning-mode (= :panning command-mode)
        drawing-mode (or (= :line command-mode) (= :arc command-mode))

        ;variables for panning
        last-off-x (om/get-state owner :before-panning-offset-x)
        last-off-y (om/get-state owner :before-panning-offset-y)
        point-of-click-x (om/get-state owner :point-of-click-x)
        point-of-click-y (om/get-state owner :point-of-click-y)
        
        ;conditions
        start-panning? (and mouse-down? panning-mode)
        is-panning? (and mouse-move? panning-mode (om/get-state owner :user-is-panning))
        was-panning? (and panning-mode (om/get-state owner :user-is-panning))
        stop-panning? (and mouse-up? was-panning?)
        start-animating? (and drawing-mode mouse-down)
        is-animating? (and )]
    (when start-panning?
      (om/set-state! owner :user-is-panning true)
      (om/set-state! owner :point-of-click-x (.-offsetX e))
      (om/set-state! owner :point-of-click-y (.-offsetY e))
      (om/set-state! owner :before-panning-offset-x (get-in @app [:canvas-offset-x]))
      (om/set-state! owner :before-panning-offset-y (get-in @app [:canvas-offset-y]))) 
    (when stop-panning?
      (om/set-state! owner :user-is-panning false)) 
    (when is-panning?
      (om/transact! app [:canvas-offset-x] (fn [_] (+ last-off-x (- (.-offsetX e) point-of-click-x))))
      (om/transact! app [:canvas-offset-y] (fn [_] (+ last-off-y (- (.-offsetY e) point-of-click-y)))))
    (when start-animating?
      )
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
      (let [mouse-chan (om/get-state owner :mouse-chan)
            command-chan (chan)
            ;command-chan (om/get-shared owner :command-chan)
            ] 
        (go
          (loop []
            (let [[v ch] (alts! [mouse-chan command-chan])]
              (when (= ch mouse-chan) (handle-mouse-event app owner v))
              (when (= ch command-chan) (handle-command app owner v))
              (recur))))))

    ;attach event listeners here.
    om/IDidMount
    (did-mount [_]
      (let [painter-watcher (om/get-node owner "painter-watcher-ref")
            mouse-chan (om/get-state owner :mouse-chan)]
        (events/listen painter-watcher "mousemove" #(put! mouse-chan %))
        (events/listen painter-watcher "mousedown" #(put! mouse-chan %))
        (events/listen painter-watcher "mouseup" #(put! mouse-chan %))))
    
    om/IRender
    (render [this]
      (let [doc-canvas-width (get-in app [:canvas-width])
            doc-canvas-height (get-in app [:canvas-height])
            zoom-factor (get-in app [:zoom-factor])
            screen-canvas-width (* doc-canvas-width zoom-factor)
            screen-canvas-height (* doc-canvas-height zoom-factor)]
        (dom/div #js {:id "painter-watcher"
                      :style #js {:width screen-canvas-width
                                  :height screen-canvas-height}
                      :ref "painter-watcher-ref"})))))
