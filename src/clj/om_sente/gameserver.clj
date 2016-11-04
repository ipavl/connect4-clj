(ns om-sente.gameserver
  (:require [irclj.core :as i]
            [irclj.parser :as p]
            [clojure.string :as str]
            [om-sente.command-handler :as ch]
            [om-sente.websocket :as ws]))

(defn join
  "Joins the specified IRC channel."
  [irc channel]
  (i/join irc channel))

(defn message
  "Send a message to a target on IRC."
  [irc target message]
  (i/message irc target message))

(defn callback
  "Handles PRIVMSG responses from IRC. Sends the message
   down the websocket for the web interface to handle."
  [irc type & s]
  ;; Hacky way to get the user's uid back from their nick
  (let [uid (last (str/split (irc :nick) #"-"))]
    (when-let [command (ch/parse-command (type :text))]
      (message irc (first (keys (irc :channels))) "it's a thing!")) ;; TODO
    (ws/chsk-send! (Integer/parseInt uid) [:test/reply (type :text)])))

(defn connect
  "Connects to the given IRC server."
  [host port nick]
  (printf "Connecting to %s on port %d...\n" host port)
  (i/connect host port nick :callbacks {:privmsg callback}))
