; Copyright (c) Andrew Young. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(comment
"Utility methods for parsing the data provided for the Semeval-2 Japanese WSD task.
Author: Andrew Young <andrew at vaelen.org>"
)

(ns org.vaelen.semeval2
  (:use ([clojure.xml :only (parse)]))
  (:require [clojure.contrib 
             [str-utils :as str-utils] 
             [zip-filter :as zip-filter] 
             [pprint :as pprint] 
             [duck-streams :as streams]
             [lazy-xml :as lazy-xml]
            ]
            [clojure.contrib.zip-filter [xml :as zip-xml]] 
            [clojure.zip])
  (:import (java.io.File))
  (:gen-class))

(defn get-text
  "Parses sample XML files and outputs the pure text, one sentence per line."
  [files]
  (for [file files]
    (do
      (println (.getPath file))
      (for [sentence (zip-xml/xml-> (clojure.zip/xml-zip (clojure.xml/parse file)) :article)]
        [1 (:tag sentence)]
      ))))



(defn get-sense-list
  "Parses a sample XML file and pulls out all of the words with multiple senses."
  [filename]
  (for [element (zip-xml/xml-> (clojure.zip/xml-zip (clojure.xml/parse (java.io.File. filename))) :mor)]
    [(zip-xml/text element) (zip-xml/attr element :sense)]
    ))

(defn map-senses [word-list]
  (loop [word-map {} word-array (first word-list) words (rest word-list)]
    (let [word (first word-array) sense (second word-array) senses (get word-map word #{})]
      (if (empty? words) word-map
          (recur (into word-map {word (conj senses sense)}) (first words) (rest words))))))

(defn get-sense-map
  "Parses a sample XML file and produces a map of word / sense-set pairs."
  [filename]
  (map-senses (get-sense-list filename)))

(defn xml-files
  "Returns a seq of xml files in a directory."
  [directory]
  (filter #(.endsWith (.getName %1) "_sample.xml") (file-seq (java.io.File. directory))))

(defn go [] 
;  (take 5 (get-text (xml-files "/home/vaelen/projects/jpdv/semeval2/sample_data")))
  (take 5 (get-text (take 1 (xml-files "/home/vaelen/projects/jpdv/semeval2/sample_data"))))
)