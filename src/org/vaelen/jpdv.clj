(comment
Sample clojure source file
)
(ns org.vaelen.jpdv
    (:gen-class))

(defn -main
    ([greetee]
  (println (str "Hello " greetee "!")))
  ([] (-main "world")))
