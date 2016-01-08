(ns editor.drawing_utilities)

(defn draw-arc
  [ctx x y rad start end ccw color]
  (set! (.-strokeStyle ctx) color)
  (.beginPath ctx)
  (.arc ctx x y rad start end ccw)
  (.stroke ctx)
  (.closePath ctx))

(defn draw-line
  [ctx x1 y1 x2 y2 color]
  (set! (.-strokeStyle ctx) color)
  (.beginPath ctx)
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2)
  (.stroke ctx)
  (.closePath ctx))

(defn draw-dot
  [ctx x y color size]
  (set! (.-fillStyle ctx) color)
  (.fillRect ctx x y size size))

(defn draw-circ
  [ctx x y color size]
  (draw-arc ctx x y size 0 (* 2 (aget js/Math "PI")) false color))

(defn abs [n] (max n (- n)))


