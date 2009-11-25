(comment
Sample clojure source file
)
(ns org.vaelen.jpdv
    (:gen-class))

(use '[clojure.xml :only (parse)]) 
(parse (java.io.File. "examples/utf-8/keio_st_overview/overview.xml"))

(defn -main
    ([greetee]
  (println (str "Hello " greetee "!")))
  ([] (-main "world")))
