(ns editor.components.commands
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [om.core :as om :include-macros true]
            [om.dom :as omdom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]))

(defn class-name-for-command
  [current-command command]
  (if (= current-command command)
    (str "command-type-item" " " "command-type-item-selected")
    "command-type-item")) 
 
(defn command-button-component
  [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [command-chan command-name css-class]}]
      (let [current-command-type (get-in app [:tools :command-mode])
            class-name (class-name-for-command current-command-type command-name)]
        (omdom/div #js {:className class-name
                        :onClick (fn [e] (put! command-chan command-name))}
                   (omdom/i #js {:className css-class}))
        ))))

(defn command-selector-component
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:command-chan (chan)
       :color-chan (chan)})

    om/IWillMount
    (will-mount [_]
      (let [command-chan (om/get-state owner :command-chan)]
        (go
          (while true
            (let [[v ch] (alts! [command-chan])]
              (when (= ch command-chan)
                (do
                  (om/update! app [:tools :command-type] v))))))))
     
    om/IRenderState
    (render-state [this {:keys [command-chan]}]
      (omdom/div #js {:className "command-menu"}
        (om/build command-button-component app {:init-state {:command-chan command-chan
                                                             :command-mode :dot
                                                             :css-class "icon-brush"}})
        (om/build command-button-component app {:init-state {:command-chan command-chan
                                                             :command-mode :line
                                                             :css-class "icon-pencil"}})
        (om/build command-button-component app {:init-state {:command-chan command-chan
                                                             :command-mode :arc
                                                             :css-class "icon-edit"}})
        (om/build command-button-component app {:init-state {:command-chan command-chan
                                                             :command-mode :panning
                                                             :css-class "icon-bucket"}})))))
