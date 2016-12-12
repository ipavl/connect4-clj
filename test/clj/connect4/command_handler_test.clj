(ns connect4.command-handler-test
  (:require [clojure.test :refer :all]
            [connect4.command-handler :refer :all]))

(deftest test-parse-command
  (testing "valid command"
    (do
      (is (= ["ACCEPT_CHALLENGE" "test"] (parse-command "ACCEPT_CHALLENGE:test")))))

  (testing "malformed command"
    (do
      (is (= nil (parse-command ":ACCEPT_CHALLENGE")))
      (is (= nil (parse-command "ACCEPT_CHALLENGE:")))))

  (testing "invalid command"
    (do
      (is (= nil (parse-command "Not a command")))
      (is (= nil (parse-command "FAKE:COMMAND"))))))
