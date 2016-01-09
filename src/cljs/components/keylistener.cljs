(ns editor.components.keylistener
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as omdom :include-macros true]
            [editor.timemachine :as timemachine]
            [cljs.core.async :refer [put! chan <! alts!]]))

(def ESC-KEY 27)
(def ONE-KEY 49)
(def TWO-KEY 50)
(def THREE-KEY 51)
(def FOUR-KEY 52)
(def FIVE-KEY 53)
(def SIX-KEY 54)
(def A-KEY 65) 
(def D-KEY 68)
(def E-KEY 69) 
(def C-KEY 67)
(def G-KEY 71)
(def Z-KEY 90)
(def X-KEY 88)
(def Q-KEY 81)
(def V-KEY 86)
(def W-KEY 87)
(def O-KEY 79)
(def MINUS-KEY 189)
(def PLUS-KEY 187)
(def LEFT-ARROW-KEY 37)
(def RIGHT-ARROW-KEY 39)
(def BACKSPACE-KEY 8)

(defn toggle-panning [app]
  (.log js/console "pan")
  (om/update! app [:drawing :mouse-mode] :panning))

(defn toggle-drawing [app] 
  (.log js/console "draw")
  (om/update! app [:drawing :mouse-mode] :drawing))
 
(defn handle-key-event [app event]
  (let [keyCode (.-keyCode event)
        metaKey (.-metaKey event)
        shiftKey (.-shiftKey event)
        ctrlKey (.-ctrlKey event)
        handler (cond
                 (and (= keyCode Z-KEY) (or ctrlKey metaKey) shiftKey)
                 #(timemachine/do-redo)
                 (and (= keyCode Z-KEY) (or ctrlKey metaKey))
                 #(timemachine/do-undo)
                 (= keyCode ESC-KEY) #(om/update! app [:drawing :element-draw-step] 1)
                 (or ctrlKey metaKey) #(toggle-panning app)
                 :else #(toggle-drawing app)
                 )]
    (when-not (= handler nil) (handler app))))

(defn key-listener-component [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:key-chan (chan)})

    om/IWillMount
    (will-mount [_]
      (let [key-chan (om/get-state owner :key-chan)] 
        (go
          (loop []
            (let [[v ch] (alts! [key-chan])]
              (when (= ch key-chan) (handle-key-event app v))
              (recur))))))

    om/IDidMount
    (did-mount [_]
      (let [key-chan (om/get-state owner :key-chan)]
        (events/listen js/document "keydown" #(put! key-chan %))
        (events/listen js/document "keyup" #(put! key-chan %)))) 

    om/IRender
    (render [this]
      (omdom/div nil ""))))
