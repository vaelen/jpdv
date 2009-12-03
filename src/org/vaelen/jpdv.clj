; Copyright (c) Andrew Young. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(comment
"A utility for generating semantic vector spaces from parsed Japanese text.
Author: Andrew Young <andrew at vaelen.org>"
)

(ns org.vaelen.jpdv
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

(defn parse-cabocha-token
  "Parses one token out of a CaboCha formatted XML file.
   This is called from parse-cabocha-chunk."
  [chunk token]
  { 
   :tag :tok
   :attrs {
           :id (zip-xml/attr token :id)
           :read (zip-xml/attr token :read)
           :base (zip-xml/attr token :base)
           :pos (zip-xml/attr token :pos)
           :ctype (zip-xml/attr token :ctype)
           :cform (zip-xml/attr token :cform)
           :ne (zip-xml/attr token :ne)
          }
   :content [(zip-xml/text token)]
  })

(defn parse-cabocha-chunk
  "Parses one chunk out of a CaboCha formatted XML file.
   This is called from parse-cabocha and recursively."
  [sentence chunk]
  { 
   :tag :chunk
   :attrs {
           :id (zip-xml/attr chunk :id)
           :link (zip-xml/attr chunk :link)
           :rel (zip-xml/attr chunk :rel)
           :score (zip-xml/attr chunk :score)
           :head (zip-xml/attr chunk :head)
           :func (zip-xml/attr chunk :func)
          }
   :content (vec (into
             (for [token (zip-xml/xml-> chunk :tok)]
               (parse-cabocha-token chunk token))
             (for [child (zip-xml/xml-> sentence :chunk (zip-xml/attr= :link (zip-xml/attr chunk :id)))]
               (parse-cabocha-chunk sentence child))))
  })

(defn parse-cabocha 
  "Parses the given XML file in CaboCha format.
   The output is an xml-zip compatible tree that represents the dependency parse."
  [filename]
  (let [xml (clojure.zip/xml-zip (clojure.xml/parse (java.io.File. filename)))]
    (if (nil? xml) nil
      { :tag :sentences :attrs nil :content (vec
        (for [sentence (zip-xml/xml-> xml :sentence)]
          { :tag :sentence :attrs nil :content (vec
            (for [chunk (zip-xml/xml-> sentence :chunk (zip-xml/attr= :link "-1"))]
              (parse-cabocha-chunk sentence chunk)))
        }))
      })))

(defn flatten 
  "Flattens an tree structure, returning all the leafs as a seq."
  [x] 
  (let [s? #(instance? clojure.lang.Sequential %)] (filter (complement s?) (tree-seq s? seq x)))) 

(defn text 
  "Returns a flat seq of all the text in a given XML element."
  [element]
  (flatten (for [x (xml-seq element) :when (= :tok (:tag x))] (:content x))))

(defn increment-count 
  "Increments the count for a given key in a map with key/count pairs."
  [map key]
  (assoc map key (inc (get map key 0))))

(defn merge-counts
  "Merges a seq of key/count maps into a single map."
  [maps]
  (if (< (count maps) 2)
    maps ; No need to merge if there is only one
    (reduce #(merge-with + %1 %2) maps)))

(defn update-context
  "Merges an existing context with a new one if necessary."
  [old-record new-record]
  (if (nil? old-record)
    new-record ; No merge needed
    {
      ; Add the values together to get a total count.
      :token (:token old-record)
      :context (if (map? (:context old-record)) (merge-with + (:context old-record) (:context new-record)) (:context old-record))
      :count (+ (:count old-record) (:count new-record))
     }))

(defn add-type
  "Adds a record to the type map."
  [types token pre-context post-context]
  (assoc types token (update-context (types token) {
                                                    :context (merge-counts (for [x (into pre-context post-context)] {x 1}))
                                                    :token token
                                                    :count 1
                                                    })))

; Context looks like this: ([...] token [...])
(defn get-start-context
  "Builds up the starting context."
  [words context-size]
  (let [start-post-context (vec (take (inc context-size) words)) start-words (drop (inc context-size) words)]
    (loop [types {} pre-context [] post-context (subvec start-post-context 1) mid (first start-post-context) token (first start-words) txt (rest start-words)]
      (if (= (count pre-context) context-size)
        {:txt txt :token token :pre-context pre-context :mid mid :post-context post-context :types types} ; Done
        (recur ; Not Done
          (add-type types mid pre-context post-context)           
          (conj pre-context mid)
          (conj (subvec post-context 1) token) 
          (first post-context)
          (first txt) 
          (rest txt))))))

(defn get-context 
  "Given a seq of words, returns the context of the previous context-size words.
   If there is more than one instance of a given word in the text, the contexts for each are combined."
  [words context-size]
  ; Start by filling the context buffer so that we can get pre and post contexts
  (let [start-context (get-start-context words (quot context-size 2))]
    (loop [types (:types start-context) 
           pre-context (:pre-context start-context)
           mid (:mid start-context)
           post-context (:post-context start-context)
           token (:token start-context)
           txt (:txt start-context)]
      (if (empty? post-context)
        (add-type types mid pre-context post-context) ; Done Recursing
        (recur ; Not Done Yet, Recurse
          (add-type types mid pre-context post-context)
          (conj (subvec pre-context 1) mid)
          (first post-context)
          (subvec (if (nil? token) post-context (conj post-context token)) 1)
          (first txt)
          (rest txt))))))

(defn get-context-space 
  "Generates a vector space based on a pre and post context window for each word type in the given XML file."
  [filename context-size]
  (let [sentences (clojure.xml/parse (java.io.File. filename))]
    (let [t (text sentences)] ; Flatten the sentences to get just the text.
      (get-context (text sentences) context-size))))

(defn sort-space
  "Sorts the space by word frequency."
  [space]
  (sort-by #(:count (second %1)) #(- 0 (compare %1 %2)) space))

(defn get-context-features
  "Returns a set of all context features (dimentions) in a given vector space"
  [space]
  (set (flatten (for [x space] (keys (:context (second x)))))))

(defn write-space
  "Writes a vector space to standard out."
  [space]
  (let [features (get-context-features space)]
    (println (format "%s\t%s" "WORD" (str-utils/str-join "\t" (sorted-set features))))
    (let [default-features (merge-counts (for [x features] {x 0}))]
      (loop [type (first space) buffer (rest space)]
        (let [type-features (into (sorted-map) (merge default-features (:context (second type))))]
          (println (format "%s\t%s" (first type) (str-utils/str-join "\t" (map second type-features))))
          (if (empty? buffer)
            nil ; Done
            (recur (first buffer) (next buffer))))))))

(defn latex-node
  "Returns the LaTeX node definition for a given node of the parse tree."
  [node children & options]
  (format "child { node {\\jptext{%s}} %s }" node children))

(defn latex-relation-node
  "Returns the LaTeX node definition for a given node of the parse tree."
  [node children & options]
  (format "child { node[blue] {\\jptext{%s}} %s }" node children))

(defn latex-chunk
  "Returns the LaTeX tree representation for a given chunk."
  [chunk]
  (let [head (zip-xml/attr chunk :head)]
    (latex-node
     (str-utils/str-join "" (for [token (zip-xml/xml-> chunk :tok)] 
                              (if (= head (zip-xml/attr token :id)) 
                                (format "\\textcolor{red}{%s}" (zip-xml/text token))
                                (zip-xml/text token))))
     (str-utils/str-join " " (for [child (zip-xml/xml-> chunk :chunk)] 
                               (latex-relation-node (zip-xml/attr child :func) (latex-chunk child)))))))

(defn latex-tree
  "Returns a LaTeX representation of a parsed dependency tree."
  [tree]
  (let [root (clojure.zip/xml-zip tree)]
    (println (str-utils/str-join "\n" 
      (for [sentence (zip-xml/xml-> root :sentence)]
        (format "\\begin{tikzpicture}[sibling distance=40mm,level distance=10mm]\n\\tikzstyle{every node}=[draw]\n\\node {S} %s ;\n\\end{tikzpicture}\n\n"
                (str-utils/str-join " " (for [chunk (zip-xml/xml-> sentence :chunk)] (latex-chunk chunk)))))))))

(defn convert-to-dependency-tree-xml
  "Reads in an xml file in CaboCha format and outputs a dependency tree."
  [input output]
  (let [tree (parse-cabocha input)]
    (streams/with-out-writer output
      (lazy-xml/emit tree))))

(defn go
  "A command for performing the default actions from a REPL."
  []
  ;(pprint/pprint (parse-cabocha "/home/vaelen/projects/jpdv/examples/simple/example.xml"))
  ;(pprint/pprint (text (parse-cabocha "/home/vaelen/projects/jpdv/examples/simple/example.xml")))
  ;(write-space (get-context-space "/home/vaelen/projects/jpdv/examples/keio_st_overview/overview.xml" 10))
  (println (latex-tree (parse-cabocha "/home/vaelen/projects/jpdv/examples/simple/example.xml")))
  ;(println (latex-tree (parse-cabocha "/home/vaelen/projects/jpdv/examples/keio_st_overview/overview.xml")))
  ;(convert-to-dependency-tree-xml "/home/vaelen/projects/jpdv/examples/simple/example.xml" *out*)

)