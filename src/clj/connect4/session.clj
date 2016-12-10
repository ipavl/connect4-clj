(ns connect4.session)

(def session-map (atom {}))
(def game-board  (atom []))

(defn store-board
  [board]
  (reset! game-board board))

(defn keep-alive
  "Given a UID, keep it alive."
  [uid]
  (println "keep-alive" uid (java.util.Date.))
  (when-let [token (get @session-map [uid :token])]
    (swap! session-map assoc-in [uid :token] token)))

(defn add-token
  "Given a UID and a token, remember it."
  [uid token]
  (println "add-token" uid token (java.util.Date.))
  (swap! session-map assoc-in [uid :token] token))

(defn add-irc-connection
  "Given a UID and an IRC connection object, remember it."
  [uid irc]
  (println "add-irc-connection" uid irc)
  (swap! session-map assoc-in [uid :irc] irc))

(defn get-token
  "Given a UID, retrieve the associated token, if any."
  [uid]
  (let [token (get-in @session-map [uid :token])]
    (println "get-token" uid token (java.util.Date.))
    token))

(defn get-irc-connection
  "Given a UID, retrieve the associated IRC connection, if any."
  [uid]
  (let [irc (get-in @session-map [uid :irc])]
    (println "get-irc-connection" uid irc)
    irc))
