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

(defmethod handle-command :play
  [params]
  (let [split (str/split (params :params) #":" 2)
        challenge-id (first split)
        col (chh/parse-int (last split))]
    (if-not (nil? col)
      (if (and @session/in-game
               (or (= (params :source) :client)
                   (= challenge-id @session/challenge-id))
               (<= 0 col p/board-width))
        (dosync
          (session/store-board
            (chh/create-updated-board
              @session/game-board
              col
              (params :source)))))))
  @session/game-board)

(defmethod handle-command :open_challenge
  [params]
  "Stores the challenge-id if the command was from the client.
   Ignores the command if it came from IRC."
  (if (= (params :source) :client)
    (let [challenge-id (params :params)]
      (dosync
        (session/store-in-game false)
        (session/store-board nil)
        (session/store-challenge-id challenge-id))))
  @session/game-board)

(defmethod handle-command :accept_challenge
  [params]
  (if-not @session/in-game
    (let [challenge-id (params :params)]
      (if (or (= (params :source) :client)
              (= challenge-id @session/challenge-id))
        (dosync
          (session/store-challenge-id challenge-id)
          (session/store-board (chh/create-game-board))
          (session/store-in-game true)))))
  @session/game-board)

(defmethod handle-command :cancel_challenge
  [params]
  "Cancels the currently open challenge."
  (if (= (params :source) :client)
    (dosync
      (session/store-in-game false)
      (session/store-board nil)
      (session/store-challenge-id nil)))
  @session/game-board)

(defmethod handle-command :resign
  [params]
  "Resigns from the current game."
  (dosync
    (session/store-in-game false)
    (session/store-challenge-id nil))
  @session/game-board)

(defmethod handle-command :debug
  [params]
  (let [command (params :params)]
    (cond
      (= command "BOARD") (println @session/game-board)
      (= command "SMAP") (println session/session-map)
      (= command "CID") (println @session/challenge-id))
    @session/game-board))

(defmethod handle-command :default
  [params]
  (println "No handler for" (params :command)))
