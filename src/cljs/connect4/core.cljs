;; copyright (c) 2014 Sean Corfield
;;
;; small demo to show Om / Sente playing together
;;
;; no claim is made of best practices - feedback welcome

(ns connect4.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [taoensso.sente :as s]
            [cljs.core.async :as async :refer [<! >! chan]]))

#_(enable-console-print!)

;; create the Sente web socket connection stuff when we are loaded:

(let [{:keys [chsk ch-recv send-fn state]}
      (s/make-channel-socket! "/ws" {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state chsk-state))

(defn field-change
  "Generic input field updater. Keeps state in sync with input."
  [e owner field]
  (let [value (.. e -target -value)]
    (om/set-state! owner field value)))

(defn send-text-on-enter
  "When user presses ENTER, send the value of the field to the server
  and clear the field's input state."
  [e owner state]
  (let [kc (.-keyCode e)
        w (.-which e)]
    (when (or (== kc 13) (== w 13))
      (chsk-send! [:test/echo (:text state)])
      (om/set-state! owner :text ""))))

(def text-length 32)

(defn text-sender
  "Component that displays a text field and sends it to the server when ENTER is pressed."
  [app owner]
  (reify
    om/IInitState
    (init-state [this]
                {:text ""})
    om/IRenderState
    (render-state [this state]
                  (html [:input {:type "text" :value (:text state) :size text-length :max-length text-length
                                 :on-change #(field-change % owner :text)
                                 :on-key-press #(send-text-on-enter % owner state)}]))))

(defn make-target
  "Turn a string into a sequence of its characters' ASCII values."
  [s]
  (take text-length (concat (map #(.charCodeAt %) s) (repeat 0))))

(defn game-board
  [app owner]
  (reify
    om/IInitState
    (init-state [this]
                {:board "localhost"})
    om/IRenderState
    (render-state [this state]
      (let [board (:board/text app)
            t (make-target board)]
        (html [:table {:style {}}
          (for [row board]
            [:tr {:style {}}
              (for [cell row]
                [:td {:style {:border "solid black 1px"
                              :width 200
                              :height 100}}
                     cell])])])))))

(defmulti handle-event
  "Handle events based on the event ID."
  (fn [[ev-id ev-arg] app owner] ev-id))

;; Process the server's reply by updating the application state:

(defmethod handle-event :game/board
  [[_ msg] app owner]
  (om/update! app :board/text msg))

;; Ignore unknown events (we just print to the console):

(defmethod handle-event :default
  [event app owner]
  #_(println "UNKNOWN EVENT" event))

;; Remember the session state in the application component's local state:

(defmethod handle-event :session/state
  [[_ state] app owner]
  (om/set-state! owner :session/state state))

;; Handle authentication failure (we just set an error message for display):

(defmethod handle-event :auth/fail
  [_ app owner]
  (om/update! app [:notify/error] "Invalid credentials"))

;; Handle authentication success (clear the error message; update application session state):

(defmethod handle-event :auth/success
  [_ app owner]
  (om/set-state! owner :session/state :secure))

(defn test-session
  "Ping the server to update the sesssion state."
  [owner]
  (chsk-send! [:session/status]))

(defn event-loop
  "Handle inbound events."
  [app owner]
  (go (loop [[op arg] (:event (<! ch-chsk))]
        #_(println "-" op)
        (case op
          :chsk/recv (handle-event arg app owner)
          ;; we ignore other Sente events
          (test-session owner))
        (recur (:event (<! ch-chsk))))))

(defn attempt-login
  "Handle the login event - send credentials to the server."
  [e app owner]
  (let [host (-> (om/get-node owner "host") .-value)
        port (-> (om/get-node owner "port") .-value)
        room (-> (om/get-node owner "room") .-value)]
    (om/update! app [:notify/error] "Connecting...")
    (chsk-send! [:session/auth [host port room]]))
  ;; suppress the form submit:
  false)

(defn login-form
  "Component that provides a login form and submits credentials to the server."
  [app owner]
  (reify
    om/IInitState
    (init-state [this]
                {:host "irc.freenode.net" :port "6667" :room "#cljtest"})
    om/IRenderState
    (render-state [this state]
                  (html [:div {:style {:margin "auto" :width "175"
                                       :border "solid blue 1px" :padding 20}}
                         (when-let [error (:notify/error app)]
                           [:div {:style #js {:color "red"}} error])
                         [:h1 "Connect"]
                         [:form {:on-submit #(attempt-login % app owner)}
                          [:div
                           [:p "Host"]
                           [:input {:ref "host" :type "text" :value (:host state)
                                    :on-change #(field-change % owner :host)}]]
                          [:div
                           [:p "Port"]
                           [:input {:ref "port" :type "number" :value (:port state)
                                    :on-change #(field-change % owner :port)}]]
                          [:div
                           [:p "Room"]
                           [:input {:ref "room" :type "text" :value (:room state)
                                    :on-change #(field-change % owner :room)}]]
                          [:div
                           [:input {:type "submit" :value "Connect"}]]]]))))

(defn secured-application
  "Component that represents the secured portion of our application."
  [app owner]
  (reify
    om/IRender
    (render [this]
            (html [:div {:style {:margin "auto" :width "1000"
                                 :border "solid blue 1px" :padding 20}}
                   [:h1 "Connect 4"]
                   (om/build text-sender app {})
                   (om/build game-board app {})
                   [:div {:style {:clear "both"}}]]))))

(defn application
  "Component that represents our application. Maintains session state.
  Selects views based on session state."
  [app owner]
  (reify
    om/IInitState
    (init-state [this]
                {:session/state :unknown})
    om/IWillMount
    (will-mount [this]
                (event-loop app owner))
    om/IRenderState
    (render-state [this state]
                  (dom/div #js {:style #js {:width "100%"}}
                           (case (:session/state state)
                             :secure
                             (om/build secured-application app {})
                             :open
                             (om/build login-form app {})
                             :unknown
                             (dom/div nil "Loading..."))))))

(def app-state
  "Our very minimal application state - a piece of text that we display."
  (atom {:data/text "Enter a string and press RETURN!"}))

(om/root application
         app-state
         {:target (. js/document (getElementById "app"))})

