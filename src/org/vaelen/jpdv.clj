(comment
Sample clojure source file
)

(ns org.vaelen.jpdv
  (:use ([clojure.xml :only (parse)])) 
  (:import (java.io.File))
    (:gen-class)
)

(defn parse-cabocha 
  "Parses the given XML file in CaboCha format.
   If no file name is given, an example is parsed."
  ([] (parse-cabocha "/home/vaelen/projects/jpdv/examples/utf-8/keio_st_overview/overview.xml"))
  ([filename] (clojure.xml/parse (java.io.File. filename)))
)

(defn flatten [x] 
  (let [s? #(instance? clojure.lang.Sequential %)] (filter (complement s?) (tree-seq s? seq x)))) 

(defn text [element]
    (flatten (for [x (xml-seq element) :when (= :tok (:tag x))] (:content x))))

(defn increment-count [map key]
  (assoc map key (+ 1 (get map key 0))))

(defn manage-previous-context 
  "Manages a previous-context buffer, adding tokens to it while maintaining a size no larger than the
   context-size"
  [context context-size token]
  (conj context token))

(defn manage-next-context 
  "Manages a next-context buffer, adding tokens to it while maintaining a size no larger than the
   context-size"
  [context context-size token]
  (conj context token))

(defn update-context
  "Merges an existing previous-context and next-context with new ones if necessary."
  [old-record new-record]
  (if (nil? old-record)
    new-record ; No merge needed
    ({
      ; Add the values together to get a total count.
      :token (:token old-record)
      :prev (merge-with #(+ %1 %2) (:prev old-record) (:prev new-record)) 
      :next (merge-with #(+ %1 %2) (:next old-record) (:next new-record)) 
      :count (+ (:count old-record) (:count new-record))
     })))

(defn get-context [sentences context-size]
  (let [t (text sentences)]
    (loop [types {} prev-context [] next-context [] token (first t) txt (rest t)]
      (if (empty? txt)
	(assoc types token (update-context (types token) {
							  :prev prev-context 
							  :next next-context
							  :token token
							  :count 1
							 }))
	(recur 
	 (assoc types token (update-context (types token) {
							   :prev prev-context 
							   :next next-context
							   :token token
							   :count 1
							   }))
	 (manage-previous-context prev-context context-size token)
	 ;(manage-next-context next-context context-size token)
	 next-context
	 (first txt) (rest txt))))))


(defn load-cabocha-file
  " Parses the given file and returns a nicer data structure."
  ([] (get-context (parse-cabocha) 5))
  ([filename] (get-context (parse-cabocha(filename) 5)))
)

