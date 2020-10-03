(ns typosquat-finder.core
  (:require [net.cgrand.enlive-html :as html]
            [clj-fuzzy.metrics :as metrics]))

(defn get-projects
  ([page-number]
   (as-> (html/html-resource
          (java.net.URL. (str "https://clojars.org/projects?page=" page-number))) %
     (map :content (html/select % [:div.result :> [(html/nth-of-type 2) :a]]))
     (flatten %)))
  ([]
   (apply concat (mapv get-projects (range 1 1352)))))

(defn get-levenshtein-neighbors [word word-list max-edit-distance]
  (as-> word-list %
    (map (fn [word-two]
           [(metrics/levenshtein word word-two)
            word-two])
         %)
    (remove (fn [[distance word]]
              (or (> distance max-edit-distance)
                  (= 0 distance) ;; take out duplicates
                  )) %)))

(defn find-neighboring-projects [project-name-list]
  (as-> project-name-list %
    (mapv
     (fn [project] [project (get-levenshtein-neighbors project project-name-list 2)])
     %)
    (remove (fn [[project neighbor-list]] (empty? neighbor-list)) %)))

;; (html/select foo [:div.result [(html/nth-of-type 2) :a]])
;; (flatten (map :content (html/select foo [:div.result :> [(html/nth-of-type 2) :a]])))



;; (def foo (vec (get-projects 1352)))

;; projects_list.end is a full pull from Clojars on 10/2/2020.
;; (def foo (clojure.edn/read-string (clojure.string/replace (slurp "projects_list.edn") #"\]\[" "")))
;; (def foo (clojure.edn/read-string (clojure.string/replace (slurp "a_projects.edn") #"\]\[" "")))
