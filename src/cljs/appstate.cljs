(ns editor.appstate)

(def app-state
  (atom
   {:canvas-width 1000
    :canvas-height 1000
    :zoom-factor 1

    ;bg image
    :canvas-offset-x 0 
    :canvas-offset-y 0
    :user-is-panning false

    :commands
    [{:element dot}
     {:element line}
     {:element polyline}
     {:element arc}
     {:element circle}
     ;{:reference fiducial}
     ;{:reference reject-mark}
     ;{:reference height}
     ;{:reference dot-size-check}
     ]

    :tools
    {:paint-color "#828282"
     :command-type :panning}
    
    :palette
    [{:color "#228751"}
     {:color "#007536"}
     {:color "#65016C"}
     {:color "#C936D3"}
     {:color "#A67A00"}
     {:color "#BF9930"}]

    :background-color "#777777"
    }))



