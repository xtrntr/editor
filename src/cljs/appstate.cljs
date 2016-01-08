(ns editor.appstate)

(def app-state
  (atom
   {:main-app
    {:canvas-width 500
     :canvas-height 500
     :zoom-factor 1
     :canvas-offset-x 0 
     :canvas-offset-y 0
     :canvas-x
     :canvas-y

     :drawing
     {:x1 0
      :y1 0
      :x2 0
      :y2 0
      :x3 0
      :y3 0
      :circ-size 0
      :element-draw-step 1 ;1 for 1st click, 2 for 2nd click, 3 for 3rd click
      :element-to-draw :line ;dot, line, arc, circle, rectangle, polyline
      :straight-line-snapping true
      } 

     :elements
     [{:type :line :x1 0 :y1 0 :x2 500 :y2 500 :color "#C936D3"}]

     :commands
     [{:element :dot}
      {:element :line}
      {:element :polyline}
      {:element :arc}
      {:element :circle}
      ]

     :tools
     {:paint-color "#828282"
      :mouse-mode :drawing}
     
     :palette
     [{:color "#228751"}
      {:color "#007536"}
      {:color "#65016C"}
      {:color "#C936D3"}
      {:color "#A67A00"}
      {:color "#BF9930"}]

     :background-image "img/circuitboard.jpg"

     
     :undo-history
     [{:action "Opened New Canvas" :icon "tag"}]
     }}))
