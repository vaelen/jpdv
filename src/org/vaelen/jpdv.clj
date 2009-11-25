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
(defn do-map-sentences 
  [sentences] (
    (println (first sentences))
    (if(seq(rest sentences)) (do-map-sentences (rest sentences)))
  )
)

(defn load-cabocha-file
  " Parses the given file and returns a nicer data structure."
  ([] (do-map-sentences(parse-cabocha)))
  ([filename] (do-map-sentences parse-cabocha(filename)))
)

