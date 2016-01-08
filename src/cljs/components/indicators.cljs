(ns editor.components.indicators
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn individual-display-component
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [information]}]
      (omdom/div #js {:className 
                      :dangerouslySetInnerHTML #js {:__html (str information)}}))))

(defn state-display-component
  [app owner]
  (reify
    om/IRender
    (render [_]
      (let [state-variables (get-in @app [:drawing])])
      (omdom/div #js {:className "display-panel"}
        (om/build individual-display-component app {:state state-variables})))))
