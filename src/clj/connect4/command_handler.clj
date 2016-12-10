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
      (session/store-board (chh/create-game-board))
      @session/game-board)
    "Handshake not OK"))

(defmethod handle-command :play
  [params]
  (let [col (Integer/parseInt (params :params))]
    (if (<= 0 col p/board-width)
      (session/store-board
        (chh/create-updated-board
          @session/game-board
          col
          (params :source))))
    @session/game-board))

(defmethod handle-command :debug
  [params]
  (let [command (params :params)]
    (cond
      (= command "BOARD") (println @session/game-board)
      (= command "SMAP") (println session/session-map))
    @session/game-board))

(defmethod handle-command :default
  [params]
  (println "No handler for" (params :command)))
