(ns editor.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [editor.appstate :as app]
            [editor.components.overlaycanvas :as overlaycanvas]
            [editor.components.canvas :as editorcanvas]
            [editor.components.mouselistener :as mousehandler]
            [editor.components.test :as test]
            [editor.components.commands :as commands]))

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
