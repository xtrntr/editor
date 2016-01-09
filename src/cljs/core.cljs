(ns editor.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as omdom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [editor.appstate :as app]
            [editor.timemachine :as timemachine]
            [editor.components.overlaycanvas :as overlaycanvas]
            [editor.components.canvas :as editorcanvas]
            [editor.components.historylist :as historylist]
            [editor.components.keylistener :as keyhandler]
            [editor.components.mouselistener :as mousehandler]
            [editor.components.test :as test]
            [editor.components.commands :as commands]))

(reset! timemachine/preview-state @app/app-state)


(defn tx-listener [tx-data root-cursor]
  (timemachine/handle-transaction tx-data root-cursor))

;; =============================================================================
;; This got out of hand before I got the hang of OM. Subsequent version will
;; place everything in a master component, so the app will ideally have one root

(defn title-component 
  [app owner]
  (reify
    om/IRender
    (render [this]
      (omdom/h1 #js {:className "app-title"}
                (:title app)
                (omdom/h6 #js {:className "app-subtitle"}
                          (str (:subtitle app) " / " (:version app)))))))

(om/root
  title-component
  app/app-state
  {:path [:info]
   :target (. js/document (getElementById "title"))
   :tx-listen #(tx-listener % %)
   })

(om/root
 editorcanvas/background-canvas-component
 app/app-state
 {:target (. js/document (getElementById "background-canvas"))})

(om/root
 overlaycanvas/overlay-canvas-component
 app/app-state
 {:target (. js/document (getElementById "overlay-canvas"))})

(om/root
 mousehandler/mouse-listener-component
 app/app-state
 {:target (. js/document (getElementById "canvas-mouse-handler"))})

(om/root
  test/palette-component
  app/app-state
  {:target (. js/document (getElementById "palette"))})

(om/root
 commands/command-selector-component
 app/app-state
 {:target (. js/document (getElementById "command-selector"))})

(om/root
 keyhandler/key-listener-component
 app/app-state
 {:target (. js/document (getElementById "global-key-handler"))})

(om/root
 historylist/history-list-component
 app/app-state
 {:target (. js/document (getElementById "undo-history"))
  :path [:main-app :undo-history]})

(om/root
 historylist/header-component
 app/app-state
 {:target (. js/document (getElementById "time-machine-header"))})
 
