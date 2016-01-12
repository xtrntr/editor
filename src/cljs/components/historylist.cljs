(ns editor.components.historylist
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as omdom :include-macros true]
            [editor.timemachine :as timemachine]
            [cljs.core.async :refer [put! chan <! alts!]]))


(defn history-list-component [undo-history owner]
  (reify
    om/IRender
    (render [this]
      (apply omdom/div #js {:className "undo-list" :transitionName "example"}
             (map-indexed
              (fn [idx history-elem]
                (let [class-name "undo-list-elem"
                      icon-class (str "icon-"  history-elem)
                      undo-history-count (count undo-history)
                      real-idx (dec (- undo-history-count idx))]
                  ;(.log js/console history-element)
                  (omdom/li #js {:className class-name
                                 :onMouseEnter #(timemachine/show-history-preview undo-history)
                                 :onMouseLeave #(timemachine/update-preview)}
                            (omdom/i #js {:className icon-class})
                            (:action history-elem))))
              (reverse undo-history))))))



(defn class-name-for-menu-item [pred]
  (if (pred) "history-menu-elem" "history-menu-elem-disabled"))

(defn header-component [app owner]
  (reify
    om/IRender
    (render [this]
      (omdom/div #js {:id "history-menu-header-text"}
                 (omdom/i #js {:className "icon-back-in-time"})
                 "History"))))

