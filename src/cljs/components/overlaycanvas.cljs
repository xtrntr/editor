(ns editor.components.overlaycanvas
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [editor.drawing :as draw]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn overlay-canvas-component  
  [app owner]   
  (reify
    om/IRender
    (render [this]
      (let [width (get-in app [:canvas-width]) 
            height (get-in app [:canvas-height])
            zoom-factor (get-in app [:zoom-factor])]
        (dom/canvas
         #js {:id "main-canvas"
              :width (* zoom-factor width)
              :height (* zoom-factor height)
              :className "canvas"
              :ref "main-canvas-ref"})))))
