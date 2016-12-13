(ns connect4.gameserver
  (:require [irclj.core :as i]
            [irclj.parser :as p]
            [clojure.string :as str]
            [connect4.command-handler :as ch]
            [connect4.websocket :as ws]))

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
      (let [reply (ch/handle-command {:id 1
                                      :uid uid
                                      :command (keyword (str/lower-case (first command)))
                                      :params (last command)
                                      :source :irc})]
        (ws/chsk-send! uid [:game/board reply])))))

(defn connect
  "Connects to the given IRC server."
  [host port nick]
  (i/connect host port nick :callbacks {:privmsg callback}))
