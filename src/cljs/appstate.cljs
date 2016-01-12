(ns editor.appstate)


(defn gen-table
  "Generate `size` rows vector of 4 columns vectors to mock up the table."
  [size]
  (mapv (fn [i] [i                                                   ; Number
                 (rand-int 1000)                                     ; Amount
                 (rand)                                              ; Coeff
                 (rand-nth ["Here" "There" "Nowhere" "Somewhere"])]) ; Store
        (range 1 (inc size))))

(def meh (gen-table 10)) 

(def app-state
  (atom
   {;; all constants here
    :canvas-width 500
    :canvas-height 500
    :img-display-width 0
    :img-display-height 0
    
    :elements
    [{:type :line :x1 0 :y1 0 :x2 500 :y2 500 :color "#C936D3"}]
    
    :commands
    [{:element :dot}
     {:element :line}
     {:element :polyline}
     {:element :arc}
     {:element :circle}]

     :info 
     {
       :title "Techno-Digm"
       :subtitle "offline programming tool"
       :version "v0.0.1"
     } 

    :table meh
  
     ;; all transient state i.e. drawing flags here.
     :drawing
     {:x1 0
      :y1 0
      :x2 0
      :y2 0
      :x3 0
      :y3 0
      :circ-size 0
      ;1 for 1st click, 2 for 2nd click, 3 for 3rd click
      :element-draw-step 1 
      ;dot, line, arc, circle, rectangle, polyline
      :element-to-draw :line
      :paint-color "#828282"
      :mouse-mode :drawing
      :straight-line-snapping true
      :zoom-factor 1
      :canvas-offset-x 0 
      :canvas-offset-y 0}

     :palette
     [{:color "#228751"}
      {:color "#007536"}
      {:color "#65016C"}
      {:color "#C936D3"}
      {:color "#A67A00"}
      {:color "#BF9930"}]
    
    ;; all state to be saved for undo/redo operations here.
    :main-app
    {:background-image "img/circuitboard.jpg"
     :elements []
     :undo-history [{:action "Opened New Canvas" :icon "tag"}]
     :selected-image [:img
                      :width 0
                      :height 0]
     }
    }))

