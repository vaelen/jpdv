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

(defn manage-context 
  "Manages a context buffer, adding tokens to it while maintaining a size no larger than the
   context-size"
  [context context-size token]
  (conj (if (>= (count context) context-size) (rest context) context) token))

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

(defn get-context 
  "Given a seq of words, returns the context of the previous context-size words.
   If there is more than one instance of a given word in the text, the contexts for each are combined."
  [words context-size]
  (loop [types {} context [] token (first words) txt (rest words)]
    (if (empty? txt)
      (assoc types token (update-context (types token) {
							:context (merge-counts (for [x context] {x 1}))
							:token token
							:count 1
							}))
      (recur 
       (assoc types token (update-context (types token) {
							 :context (merge-counts (for [x context] {x 1}))
							 :token token
							 :count 1
							 }))
       (manage-context context context-size token)
       (first txt) (rest txt)))))

(defn get-context-space 
  "Generates a pre context window for each word type in the given seq of sentence trees."
  [sentences context-size]
  (let [t (text sentences)] ; Flatten the sentences to get just the text.
    (get-context (text sentences) context-size)))


(defn load-cabocha-file
  "Parses the given file and returns a nicer data structure."
  ([] (get-context-space (parse-cabocha) 5))
  ([filename] (get-context (parse-cabocha(filename) 5)))
)

(defn sort-space
  "Sorts the space by word frequency."
  [space]
  (sort-by #(:count (second %1)) #(- 0 (compare %1 %2)) space))