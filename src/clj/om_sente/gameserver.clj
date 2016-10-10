(ns om-sente.gameserver
  (:require [irclj.core :as i]))

(defn callback
  [irc type & s]
  (prn irc type s))

(defn connect
  "Connects to the given IRC server."
  [host port nick channel]
  (printf "Connecting to %s on port %d...\n" host port)
  (def irc (i/connect host port nick :callbacks {:privmsg callback}))
  (i/join irc "#iantest"))

