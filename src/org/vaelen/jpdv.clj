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

(defn do-map-sentences [sentences]
  (let [t (text sentences)]
    (loop [types {} token (first t) txt (rest t)]
;      (println token)
      (if (empty? txt)
	(increment-count types token)
	(recur (increment-count types token) (first txt) (rest txt))))))


(defn load-cabocha-file
  " Parses the given file and returns a nicer data structure."
  ([] (do-map-sentences (parse-cabocha)))
  ([filename] (do-map-sentences (parse-cabocha(filename))))
)

