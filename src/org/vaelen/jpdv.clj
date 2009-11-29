(comment
"A utility for generating semantic vector spaces from parsed Japanese text.
Author: Andrew Young <andrew at vaelen.org>"
)

(ns org.vaelen.jpdv
  (:use ([clojure.xml :only (parse)])) 
  (:import (java.io.File))
    (:gen-class)
)

(defn parse-cabocha 
  "Parses the given XML file in CaboCha format.
   If no file name is given, an example file is parsed."
  ([] (parse-cabocha "/home/vaelen/projects/jpdv/examples/utf-8/keio_st_overview/overview.xml"))
  ([filename] (clojure.xml/parse (java.io.File. filename)))
)

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
  "Generates a pre context window for each word type in the given seq of sentence trees."
  [sentences context-size]
  (let [t (text sentences)] ; Flatten the sentences to get just the text.
    (get-context (text sentences) context-size)))


(defn load-cabocha-file
  "Parses the given file and returns a nicer data structure."
  ([] (get-context-space (parse-cabocha) 10))
  ([filename] (get-context-space (parse-cabocha(filename) 10)))
)

(defn sort-space
  "Sorts the space by word frequency."
  [space]
  (sort-by #(:count (second %1)) #(- 0 (compare %1 %2)) space))