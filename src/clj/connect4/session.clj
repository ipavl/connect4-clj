;; Portions of the code in this file is either from or based on
;; https://github.com/seancorfield/om-sente, which is licensed
;; under the EPL-1.0 and copyright (c) 2014 Sean Corfield.

(ns connect4.session)

(def session-map  (atom {}))

(def game-board   (ref []))
(def challenge-id (ref nil))
(def in-game      (ref false))

(defn store-board
  [board]
  (ref-set game-board board))

(defn store-challenge-id
  [cid]
  (ref-set challenge-id cid))

(defn store-in-game
  [status]
  (ref-set in-game status))

(defn keep-alive
  "Given a UID, keep it alive."
  [uid]
  (when-let [token (get @session-map [uid :token])]
    (swap! session-map assoc-in [uid :token] token)))

(defn add-token
  "Given a UID and a token, remember it."
  [uid token]
  (swap! session-map assoc-in [uid :token] token))

(defn add-irc-connection
  "Given a UID and an IRC connection object, remember it."
  [uid irc]
  (swap! session-map assoc-in [uid :irc] irc))

(defn get-token
  "Given a UID, retrieve the associated token, if any."
  [uid]
  (get-in @session-map [uid :token]))

(defn get-irc-connection
  "Given a UID, retrieve the associated IRC connection, if any."
  [uid]
  (get-in @session-map [uid :irc]))
