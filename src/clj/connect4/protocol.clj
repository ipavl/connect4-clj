(ns connect4.protocol)

(def version "V1")

(def board-width 7)

(def board-height 6)

(def commands ["DEBUG"
               "PLAY"
               "OPEN_CHALLENGE"
               "ACCEPT_CHALLENGE"
               "CANCEL_CHALLENGE"
               "RESIGN"])
