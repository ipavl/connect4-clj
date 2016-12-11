# connect4-clj

An IRC-based Connect 4 client written in Clojure/ClojureScript using Om and Sente.

# Usage

    lein cljsbuild [once|auto]
    lein run

The default port is 8444. To change the port the server runs on, use `PORT=#### lein run` instead.

# License

Distributed under the Eclipse Public License, the same as Clojure.

# Acknowledgements

Portions of the code in this project for communication and the user interface is either from or based on https://github.com/seancorfield/om-sente, which is licensed under the EPL-1.0 and copyright (c) 2014 Sean Corfield.
