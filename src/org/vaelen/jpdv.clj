(comment
Sample clojure source file
)

(ns org.vaelen.jpdv
  (:use ([clojure.xml :only (parse)])) 
  (:import (java.io.File))
    (:gen-class))


(defn parse-cabocha 
  "Parses the given XML file in CaboCha format.
   If no file name is given, an example is parsed."
  ([] (parse-cabocha "examples/utf-8/keio_st_overview/overview.xml"))
  ([filename] (parse (java.io.File. filename)))
)

