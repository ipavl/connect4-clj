(ns connect4.command-handler-helpers-test
  (:require [clojure.test :refer :all]
            [connect4.command-handler-helpers :refer :all]))

(deftest test-create-game-board
  (testing "create game board"
    (do
      (is (= (create-game-board) [[nil nil nil nil nil nil nil]
                                  [nil nil nil nil nil nil nil]
                                  [nil nil nil nil nil nil nil]
                                  [nil nil nil nil nil nil nil]
                                  [nil nil nil nil nil nil nil]
                                  [nil nil nil nil nil nil nil]])))))

(deftest test-get-next-available-row
  (testing "fresh board"
    (do
      (is (= 5 (get-next-available-row [[nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]] 4)))))

  (testing "column with 3 items high"
    (do
      (is (= 2 (get-next-available-row [[nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil nil nil nil]
                                        [nil nil nil nil "X" nil nil]
                                        [nil nil nil nil "X" nil nil]
                                        [nil nil nil nil "X" nil nil]] 4)))))

  (testing "full column"
    (do
      (is (= nil (get-next-available-row [[nil nil nil nil "X" nil nil]
                                          [nil nil nil nil "X" nil nil]
                                          [nil nil nil nil "X" nil nil]
                                          [nil nil nil nil "X" nil nil]
                                          [nil nil nil nil "X" nil nil]
                                          [nil nil nil nil "X" nil nil]] 4))))))

(deftest test-create-updated-board
  (testing "add an item to a fresh board"
    (do
      (is (= (create-updated-board [[nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]] 4 "X") [[nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil "X" nil nil]]))))

  (testing "add an item to a partially filled column"
    (do
      (is (= (create-updated-board [[nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil nil nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]] 4 "X") [[nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil nil nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]]))))

  (testing "add an item to a full column"
    (do
      (is (= (create-updated-board [[nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]
                                    [nil nil nil nil "X" nil nil]] 4 "X") [[nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]
                                                                           [nil nil nil nil "X" nil nil]])))))
