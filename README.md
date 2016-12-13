# connect4-clj

An IRC-based Connect 4 webclient written in Clojure/ClojureScript using Om and Sente.

# Usage

It's easiest to run the project using [Leiningen](http://leiningen.org/):

    lein cljsbuild [once|auto]
    lein run

The default port is 8444. To change the port the server runs on, use `PORT=#### lein run` instead.

Tests can be run using `lein test`.

# Playing a Game

1. Launch the server (one per player if playing on the same computer)
2. Open http://localhost:8444 (or the chosen port) in a web browser
3. Fill out the form to connect to an IRC server (it may take around 10 seconds to actually connect and join the channel before you can send/receive commands - you can check in an actual IRC client)
4. Input a string into the "challenge-id" textbox and click "accept" to start a game with that ID (assumes there's another client also playing with that ID), or "open" to wait for someone to accept with your chosen ID
5. Once a game is started (an empty grid will show), click "Drop" to place a piece in that column

The buttons on the interface work based on the value entered into the challenge-id field (case sensitive):

* **Open**: will listen for ACCEPT_CHALLENGE commands with the chosen challenge-id
* **Cancel**: stops listening for the challenge-id or ends the current game (clears board)
* **Accept**: start a game with the chosen challenge-id
* **Resign**: forfeit the current match (does not clear board)

## Stubbing the client

If not using a second client, manually entering commands in an IRC channel that the game client has joined will also work. The following are the main ones:

* ACCEPT_CHALLENGE:challenge-id - start a game
* PLAY:challenge-id:column - drop a piece into a column (0-6)
* RESIGN:challenge-id - quit the match

# Limitations

One instance of the server is required per client.

There is currently no logic to check the board for wins, or handling wins or turns. It is up to the players to coordinate as they would with a physical board.

# License

Distributed under the Eclipse Public License, the same as Clojure.

# Acknowledgements

Portions of the code in this project for communication and the user interface is either from or based on https://github.com/seancorfield/om-sente, which is licensed under the EPL-1.0 and copyright (c) 2014 Sean Corfield.
