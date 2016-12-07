(ns connect4.command-handler-helpers
  (require [clojure.string :as str]
           [connect4.protocol :as p]))

(defn create-game-board
  "Creates an empty game board."
  []
  (vec (repeat p/board-height (vec (repeat p/board-width nil)))))
