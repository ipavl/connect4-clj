(ns connect4.views.layout
  (:use [hiccup.page :only (html5 include-css include-js)]))

(defn page [title & content]
  (html5 {:lang "en"}
    [:head
    [:title title]

    [:body
      content
      (include-js "https://cdnjs.cloudflare.com/ajax/libs/react/0.11.2/react.js")
      (include-js "out/goog/base.js")
      (include-js "connect4.js")
      [:script
        {:type "text/javascript"} "goog.require(\"connect4.core\");"]]]))
