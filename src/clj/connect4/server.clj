;; copyright (c) 2014 Sean Corfield
;;
;; small demo to show Om / Sente playing together
;;
;; no claim is made of best practices - feedback welcome

(ns connect4.server
  (:require [clojure.core.async :as async
             :refer [<! <!! chan go thread]]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST routes]]
            [compojure.handler :as h]
            [compojure.route :as r]
            [org.httpkit.server :as kit]
            [ring.middleware.reload :as reload]
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
  "Return a really unique ID (for an unsecured session ID).
  No, a random number is not unique enough. Use a UUID for real!"
  []
  (rand-int 10000))

(defn session-uid
  "Convenient to extract the UID that Sente needs from the request."
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

;; minimal set of routes to handle:
;; - home page request
;; - web socket GET/POST
;; - general files (mainly JS)
;; - 404

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
  "Tell the server what state this user's session is in."
  [req]
  (when-let [uid (session-uid req)]
    (ws/chsk-send! uid [:session/state (if (session/get-token uid) :secure :open)])))

;; Reply with the session state - either open or secure.

(defmethod handle-event :session/status
  [_ req]
  (session-status req))

;; Reply with authentication failure or success.
;; For a successful authentication, remember the login.

(defmethod handle-event :session/auth
  [[_ [host port room]] req]
  (when-let [uid (session-uid req)]
    ;; TODO: Handle this better
    (let [port-int (Integer/parseInt port)]
      (let [valid (and (not (str/blank? host))
                       (not (= 0 port-int))
                       (not (str/blank? room)))]
        (when valid
          (session/add-token uid (unique-id))
          (let [irc (gs/connect host port-int (str "c4-clj-" uid))]
            (session/add-irc-connection uid irc))
          (gs/join (session/get-irc-connection uid) (str "#" room))
        (ws/chsk-send! uid [(if valid :auth/success :auth/fail)]))))))

;; Reply with the same message, followed by the reverse of the message a few seconds later.
;; Also record activity to keep session alive.

(defmethod handle-event :test/echo
  [[_ msg] req]
  (when-let [uid (session-uid req)]
    (session/keep-alive uid)
    (send-irc-message uid msg)
    (ws/chsk-send! uid [:game/board msg])
    (Thread/sleep 3000)
    (ws/chsk-send! uid [:game/board (clojure.string/reverse msg)])))

;; When the client pings us, send back the session state:

(defmethod handle-event :chsk/ws-ping
  [_ req]
  (session-status req))

;; Handle unknown events.
;; Note: this includes the Sente implementation events like:
;; - :chsk/uidport-open
;; - :chsk/uidport-close

(defmethod handle-event :default
  [event req]
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
  (let [port (or (System/getenv "PORT") 8444)]
    (println "Starting Connect4 server on port" port "...")
    (kit/run-server (reload/wrap-reload #'server) {:port port})))
