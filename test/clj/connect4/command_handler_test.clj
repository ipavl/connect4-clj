(ns connect4.command-handler-test
  (:require [clojure.test :refer :all]
            [connect4.command-handler :refer :all]))

(deftest test-parse-command
  (testing "valid command"
    (do
      (is (= ["HANDSHAKE" "V1"] (parse-command "HANDSHAKE:V1")))))

  (testing "malformed command"
    (do
      (is (= nil (parse-command ":HANDSHAKE")))
      (is (= nil (parse-command "HANDSHAKE:")))))

  (testing "invalid command"
    (do
      (is (= nil (parse-command "Not a command")))
      (is (= nil (parse-command "FAKE:COMMAND"))))))
