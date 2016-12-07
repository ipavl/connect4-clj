;; session cache to maintain authentication - so we can rely
;; entirely on socket communication instead of needing to login
;; to the application first: 5 minutes of inactive will log you out

(ns connect4.session
  (:require [clojure.core.cache :as cache]))

(def session-map (atom (cache/ttl-cache-factory {} :ttl (* 5 60 1000))))

(defn keep-alive
  "Given a UID, keep it alive."
  [uid]
  (println "keep-alive" uid (java.util.Date.))
  (when-let [token (get @session-map uid)]
    (swap! session-map assoc uid token)))

(defn add-token
  "Given a UID and a token, remember it."
  [uid token]
  (println "add-token" uid token (java.util.Date.))
  (swap! session-map assoc uid token))

(defn add-irc-connection
  "Given a UID and an IRC connection object, remember it."
  [uid irc]
  (println "add-irc-connection" uid irc)
  (swap! session-map assoc uid irc))

(defn add-game-board
  "Given a UID and a game board object, remember it."
  [uid board]
  (println "add-game-board" uid board)
  (swap! session-map assoc uid board))

(defn get-token
  "Given a UID, retrieve the associated token, if any."
  [uid]
  (let [token (get @session-map uid)]
    (println "get-token" uid token (java.util.Date.))
    token))

(defn get-irc-connection
  "Given a UID, retrieve the associated IRC connection, if any."
  [uid]
  (let [irc (get @session-map uid)]
    (println "get-irc-connection" uid irc)
    irc))

(defn get-game-board
  "Given a UID, retrieve the associated game board, if any."
  [uid]
  (let [board (get @session-map uid)]
    (println "get-game-board" uid board)
    board))
