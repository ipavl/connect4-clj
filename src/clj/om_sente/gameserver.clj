(ns om-sente.gameserver
  (:require [irclj.core :as i]))

(defn callback
  [irc type & s]
  (prn irc type s))

(defn connect
  "Connects to the given IRC server."
  [host port nick]
  (printf "Connecting to %s on port %d...\n" host port)
  (i/connect host port nick :callbacks {:privmsg callback}))

(defn join
  "Joins the specified IRC channel."
  [irc channel]
  (i/join irc channel))

