(ns editor.components.table
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]
            [cljsjs.fixed-data-table]))

(def Table (js/React.createFactory js/FixedDataTable.Table))
(def Column (js/React.createFactory js/FixedDataTable.Column))
(def ColumnGroup (js/React.createFactory js/FixedDataTable.ColumnGroup))

;;; using custom :cellDataGetter in column for cljs persistent data structure
;;; is more efficient than converting row to js array in table's :rowGetter
(defn getter [k row] (get row k))

(defn table [{:keys [table]} _]
  (reify om/IRender
    (render [_]
      (Table
       #js {:width        600
            :height       400
            :rowHeight    50
            :rowGetter    #(get table %)
            :rowsCount    (count table)
            :headerHeight 50}
       (Column
        #js {:label "Number" :dataKey 0 :cellDataGetter getter :width 100})
       (Column
        #js {:label "Amount" :dataKey 1 :cellDataGetter getter :width 100})
       (Column
        #js {:label "Coeff" :dataKey 2 :cellDataGetter getter :width 200})
       (Column
        #js {:label "Store" :dataKey 3 :cellDataGetter getter :width 200})))))
