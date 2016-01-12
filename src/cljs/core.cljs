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
            [editor.components.table :as table]
            [editor.components.commands :as commands]
            [cljsjs.reactabular]
            [cljsjs.fixed-data-table]
            ))

(reset! timemachine/preview-state @app/app-state)

(defn tx-listener [tx-data root-cursor]
  (timemachine/handle-transaction tx-data root-cursor))

(defn title-component 
  [app owner]
  (reify
    om/IRender
    (render [this]
      (omdom/h1 #js {:id "app-title"}
                (:title app)
                (omdom/h6 #js {:id "app-subtitle"}
                          (str (:subtitle app) " / " (:version app)))))))

(comment)
(om/root
 title-component
 app/app-state
 {:path [:info]
  :target (. js/document (getElementById "title"))
  :tx-listen #(tx-listener % %)
  })

(defn screen 
  [app owner]
  (om/component
   (omdom/div #js {:id "main"}
              (om/build
               title-component
               (get-in app [:info]))
              (om/build 
               editorcanvas/background-canvas-component
               app)
              (om/build
               overlaycanvas/overlay-canvas-component
               app)
              (om/build
               mousehandler/mouse-listener-component
               app)
              (om/build
               test/palette-component
               app)
              (om/build
               commands/command-selector-component
               app)
              (om/build
               keyhandler/key-listener-component
               app)
              (omdom/div #js {:id "TimeMachineContainer"}
                         (om/build
                          historylist/header-component
                          app)
                         (om/build 
                          historylist/history-list-component
                           (get-in @app [:main-app :undo-history]))
                         ))))

(om/root screen app/app-state 
         {:target (. js/document (getElementById "app"))}) 


(comment

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
   commands/command-selector-componentx
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
  
  )
