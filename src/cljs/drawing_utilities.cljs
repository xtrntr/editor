(ns editor.drawing_utilities)

(defn draw-line
  [ctx x1 y1 x2 y2 color]
  (set! (.-strokeStyle ctx) color)
  (.beginPath ctx)
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2)
  (.closePath ctx)
  (.stroke ctx))

(defn draw-dot
  [ctx x y color size]
  (set! (.-fillStyle ctx) color)
  (.fillRect x y size size))

