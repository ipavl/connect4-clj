(ns connect4.command-handler-helpers
  (require [clojure.string :as str]
           [connect4.protocol :as p]))

(defn create-game-board
  "Creates an empty game board."
  []
  (vec (repeat p/board-height (vec (repeat p/board-width nil)))))

(defn get-next-available-row
  "Loops over the rows to find the lowest available square for a given column."
  [col]
  5)

(defn create-updated-board
  "Given a board and a column to add to, creates a new board."
  [board col]
  (let [row 5]
    (assoc-in board [row col] "X")))
