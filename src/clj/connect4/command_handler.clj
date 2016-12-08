(ns connect4.command-handler
  (require [clojure.string :as str]
           [connect4.session :as session]
           [connect4.command-handler-helpers :as chh]
           [connect4.protocol :as p]))

(defn parse-command
  "Given the text from an IRC message, returns the split command
   and argument as a 2-value list if the text is a valid command,
   or nil otherwise."
  [text]
  (if (.contains text ":")
    (let [parsed (str/split text #":" 2)]
      (if (and (not-any? str/blank? parsed)
               (some (partial = (first parsed)) p/commands))
        parsed))))

(defmulti handle-command
  (fn [command] (command :command)))

(defmethod handle-command :handshake
  [params]
  (if (= (params :params) p/version)
    (do
      (session/add-game-board (params :uid) (chh/create-game-board))
      (session/get-game-board (params :uid)))
    "Handshake not OK"))

(defmethod handle-command :play
  [params]
  (let [col (Integer/parseInt (params :params))]
    (if (<= 0 col p/board-width)
      (session/add-game-board
        (params :uid)
        (chh/create-updated-board
          (session/get-game-board (params :uid))
          col)))
    (session/get-game-board (params :uid))))

(defmethod handle-command :debug
  [params]
  (if (= (params :params) "BOARD")
    (session/get-game-board (params :uid))))

(defmethod handle-command :default
  [params]
  (println "No handler for" (params :command)))
