(ns om-sente.views.layout
  (:use [hiccup.page :only (html5 include-css include-js)]))

(defn page [title & content]
  (html5 {:lang "en"}
    [:head
    [:title title]

    [:body
      content
      (include-js "http://fb.me/react-0.11.2.js")
      (include-js "out/goog/base.js")
      (include-js "om_sente.js")
      [:script
        {:type "text/javascript"} "goog.require(\"om_sente.core\");"]]]))
