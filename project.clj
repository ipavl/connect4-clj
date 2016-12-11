(defproject connect4 "0.1.0-SNAPSHOT"
  :description "An IRC-based Connect 4 client."
  :url ""

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "0.0-2356"]
                 [org.clojure/core.async "0.2.395"]
                 [om "0.7.3"]
                 [sablono "0.2.22"]
                 [com.taoensso/sente "1.2.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-devel "1.5.0"]
                 [jetty/javax.servlet "5.1.12"]
                 [irclj "0.5.0-alpha4"]]

  :main "connect4.server"

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src/clj"]
  :test-paths ["test/clj"] 

  :cljsbuild {
    :builds [{:id "connect4"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "connect4.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
