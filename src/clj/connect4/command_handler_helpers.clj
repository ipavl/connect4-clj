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
    (if (<= 0 x)
      (if (nil? (get-in board [x col]))
        x
        (recur (dec x))))))

(defn create-updated-board
  "Given a board, a column to add to, and the colour of the piece to add,
   creates a new board. If the given column is at its maximum height,
   returns the old board."
  [board col colour]
  (if-let [row (get-next-available-row board col)]
    (assoc-in board [row col] colour)
    board))

(defn parse-int
  "Parses the given string as an integer. Returns nil if doing so throws an exception."
  [val]
  (try
    (Integer/parseInt val)
    (catch Exception e nil)))
