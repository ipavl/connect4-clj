;; Portions of the code in this file is either from or based on
;; https://github.com/seancorfield/om-sente, which is licensed
;; under the EPL-1.0 and copyright (c) 2014 Sean Corfield.

(ns connect4.websocket
  (:require [taoensso.sente :as s]))

;; create the Sente web socket connection stuff when we are loaded:

(let [{:keys [ch-recv send-fn ajax-post-fn
              ajax-get-or-ws-handshake-fn] :as sente-info}
      (s/make-channel-socket! {})]
  (def ring-ajax-post   ajax-post-fn)
  (def ring-ajax-get-ws ajax-get-or-ws-handshake-fn)
  (def ch-chsk          ch-recv)
  (def chsk-send!       send-fn))

