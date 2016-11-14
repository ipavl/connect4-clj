(ns om-sente.command-handler
  (require [clojure.string :as str]
           [om-sente.protocol :as p]))

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
  [arg]
  (println arg))

(defmethod handle-command :default
  [params]
  (println "No handler for" (params :command)))
