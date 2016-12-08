(ns connect4.command-handler-helpers
  (require [clojure.string :as str]
           [connect4.protocol :as p]))

(defn create-game-board
  "Creates an empty game board."
  []
  (vec (repeat p/board-height (vec (repeat p/board-width nil)))))

(defn get-next-available-row
  "Loops over the rows to find the lowest available square for a given column.
   Uses a bottom-up approach."
  [board col]
  (loop [x (- p/board-height 1)]
    (if (nil? (get-in board [x col]))
      x
      (recur (dec x)))))

(defn create-updated-board
  "Given a board and a column to add to, creates a new board."
  [board col]
  (let [row (get-next-available-row board col)]
    (assoc-in board [row col] "X")))
