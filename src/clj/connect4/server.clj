;; Portions of the code in this file is either from or based on
;; https://github.com/seancorfield/om-sente, which is licensed
;; under the EPL-1.0 and copyright (c) 2014 Sean Corfield.

(ns connect4.server
  (:require [clojure.core.async :as async
             :refer [<! <!! chan go thread]]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST routes]]
            [compojure.handler :as h]
            [compojure.route :as r]
            [org.httpkit.server :as kit]
            [ring.middleware.reload :as reload]
            [connect4.command-handler :as ch]
            [connect4.websocket :as ws]
            [connect4.session :as session]
            [connect4.gameserver :as gs]
            [connect4.views.layout :as layout]
            [connect4.views.contents :as contents]))

(defn root
  "Return the absolute (root-relative) version of the given path."
  [path]
  (str (System/getProperty "user.dir") path))

(defn unique-id
  "Use the first part of a UUID as a unique id, which will also be
   used in the client's IRC nickname. This should rarely clash."
  []
  (first (str/split (str (java.util.UUID/randomUUID)) #"-")))

(defn session-uid
  "Extract UID from request."
  [req]
  (get-in req [:session :uid]))

(defn send-irc-message
  "Helper to send messages to IRC as the specified client."
  [uid msg]
  (let [irc (session/get-irc-connection uid)]
    (gs/message irc (first (keys (irc :channels))) msg)))

(defn index
  "Handle index page request. Injects session uid if needed."
  [req]
  {:status 200
   :session (if (session-uid req)
              (:session req)
              (assoc (:session req) :uid (unique-id)))
   :body (layout/page "Connect4-clj Webclient" (contents/index))})

(defroutes server
  (-> (routes
       (GET  "/"   req (#'index req))
       (GET  "/ws" req (#'ws/ring-ajax-get-ws req))
       (POST "/ws" req (#'ws/ring-ajax-post   req))
       (r/files "/" {:root (root "")})
       (r/not-found (layout/page "Page not found" (contents/not-found))))
      h/site))

(defmulti handle-event
  "Handle events based on the event ID."
  (fn [[ev-id ev-arg] ring-req] ev-id))

(defn session-status
  "Determine which state the session is currently in."
  [req]
  (when-let [uid (session-uid req)]
    (ws/chsk-send! uid [:session/state (if (session/get-token uid) :secure :open)])))

(defmethod handle-event :session/status
  [_ req]
  "Respond with the current session state."
  (session-status req))

(defmethod handle-event :session/auth
  [[_ [host port room]] req]
  "Connect to the specified IRC server."
  (when-let [uid (session-uid req)]
    ;; TODO: Handle this better
    (let [port-int (Integer/parseInt port)]
      (let [valid (and (not (str/blank? host))
                       (not (= 0 port-int))
                       (not (str/blank? room)))]
        (when valid
          (session/add-token uid (unique-id))
          (let [irc (gs/connect host port-int (str "c4-clj--" uid))]
            (session/add-irc-connection uid irc))
          (gs/join (session/get-irc-connection uid) (str "#" room))
          (ws/chsk-send! uid [(if valid :auth/success :auth/fail)]))))))

(defmethod handle-event :test/echo
  [[_ msg] req]
  "Send the message to IRC."
  (when-let [uid (session-uid req)]
    (session/keep-alive uid)
    (send-irc-message uid msg)))

(defmethod handle-event :game/board-action
  [[_ msg] req]
  "Handle commands sent by the client's board."
  (when-let [uid (session-uid req)]
    (session/keep-alive uid)
    (when-let [command (ch/parse-command msg)]
      (let [new-board (ch/handle-command {:id 1
                                          :uid uid
                                          :command (keyword (str/lower-case (first command)))
                                          :params (last command)
                                          :source :client})]
        (send-irc-message uid msg)
        (ws/chsk-send! uid [:game/board new-board])))))

(defmethod handle-event :chsk/ws-ping
  [_ req]
  "When the client pings us, send back the session state."
  (session-status req))

(defmethod handle-event :default
  [event req]
  "Handle unknown events. Includes Sente events like :chsk/uidport-open
   and :chsk/uidport-close."
  nil)

(defn event-loop
  "Handle inbound events."
  []
  (go (loop [{:keys [client-uuid ring-req event] :as data} (<! ws/ch-chsk)]
        (println "-" event)
        (thread (handle-event event ring-req))
        (recur (<! ws/ch-chsk)))))

(defn -main
  "Start the http-kit server. Takes no arguments.
  Environment variable PORT can override default port of 8444."
  [& args]
  (event-loop)
  (let [port (if-not (nil? (System/getenv "PORT"))
               (Integer/parseInt (System/getenv "PORT"))
               8444)]
    (println "Starting Connect4 server on port" port "...")
    (kit/run-server (reload/wrap-reload #'server) {:port port})))
