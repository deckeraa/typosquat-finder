(ns typosquat-finder.core
  (:require [net.cgrand.enlive-html :as html]))

(defn get-projects
  ([page-number]
   (as-> (html/html-resource
          (java.net.URL. (str "https://clojars.org/projects?page=" page-number))) %
     (map :content (html/select % [:div.result :> [(html/nth-of-type 2) :a]]))
     (flatten %)))
  ([]
   (apply concat (mapv get-projects (range 1 3)))))

;; (html/select foo [:div.result [(html/nth-of-type 2) :a]])
;; (flatten (map :content (html/select foo [:div.result :> [(html/nth-of-type 2) :a]])))

(defonce foo (get-page))



