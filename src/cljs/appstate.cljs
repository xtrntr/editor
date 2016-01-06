(ns editor.appstate)

(def app-state
  (atom
   {:canvas-width 500
    :canvas-height 500

    ;for zooming
    :zoom-factor 1

    ;bg image
    :canvas-offset-x 0 
    :canvas-offset-y 0

    ;for drawing
    :drawing
    {:x1 0
     :y1 0
     :x2 0
     :y2 0
     :x3 0
     :y3 0
     :element-draw-step 1 ;1 for 1st click, 2 for 2nd click, 3 for 3rd click
     :element-to-draw :line ;dot, line, arc, circle, rectangle, polyline
     }

    :elements
    [{:type :line :x1 0 :y1 0 :x2 500 :y2 500 :color "#C936D3"}]

    :commands
    [{:element :dot}
     {:element :line}
     {:element :polyline}
     {:element :arc}
     {:element :circle}
     ;{:reference fiducial}
     ;{:reference reject-mark}
     ;{:reference height}
     ;{:reference dot-size-check}
     ]

    :tools
    {:paint-color "#828282"
     :command-mode :drawing}
    
    :palette
    [{:color "#228751"}
     {:color "#007536"}
     {:color "#65016C"}
     {:color "#C936D3"}
     {:color "#A67A00"}
     {:color "#BF9930"}]

    :background-image "img/circuitboard.jpg"
    }))

