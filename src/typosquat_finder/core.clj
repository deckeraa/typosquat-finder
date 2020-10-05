(ns typosquat-finder.core
  (:require [net.cgrand.enlive-html :as html]
            [clj-fuzzy.metrics :as metrics]))

(defn summarize-project [enlive-project-div]
  (let [project-name (-> enlive-project-div
                      (:content)
                      (second)
                      (:content)
                      (first))
        username (->
                  (html/select enlive-project-div [:span.details :a])
                  (first)
                  (:content)
                  (first))]
    {:name project-name
     :user username}))

(defn get-projects
  "Scrapes a list of projects from Clojars.org using summarize-project.
   If passed a page number, will retrieve only projects from that page;
   otherwise will retrieve all projects known on 2020/10/3."
  ([page-number]
   (as-> (html/html-resource
          (java.net.URL. (str "https://clojars.org/projects?page=" page-number))) %
     (map summarize-project (html/select % [:div.result]))
     (flatten %)))
  ([]
   (apply concat (mapv get-projects (range 1 1352)))))

(defn get-levenshtein-neighbors
  "Takes a project map and a list of project maps and returns all potential matches using the max-edit-distance threshold."
  [{:keys [name user] :as project} project-list max-edit-distance]
  (as-> project-list %
    (filter (fn [{:keys [name]}] (> (count name) 4)) %) ;; take out any projects with names not longer than 4 characters -- from manual review, there are a fair amount of short-named projects (probably test uploads) that are unlikely to be typosquatted upon
    (map (fn [project-two]
           [(metrics/levenshtein name (:name project-two))
            project-two])
         %)
    (remove (fn [[distance project-two]]
              (or (> distance max-edit-distance)
                  (= 0 distance) ;; take out duplicates
                  (= user (:user project-two)) ;; people aren't going to typo-squat themselves (and if they were going to, why wouldn't they just inject their malware in the real library? Clojars uses signed uploads so we can rely on the username.
                  )) %)))

(defn find-neighboring-projects
  "Runs get-levenshtein-neighbors against list of projects.
  If passed two lists, will check all members of member-list against all members of full-list.
  This lets you analyze the top N libraries by popularity."
  ([project-list]
   (find-neighboring-projects project-list project-list))
  ([project-list full-list]
   (as-> project-list %
     (mapv
      (fn [project] [project (get-levenshtein-neighbors project full-list 2)])
      %)
     (remove (fn [[project neighbor-list]] (empty? neighbor-list)) %))))

;; Download statistics are from
;; https://github.com/clojars/clojars-web/wiki/Data
(def sample-download-count-entry
  [["duct.logger.honeybadger" "duct.logger.honeybadger"]
   {"0.1.0" 29, "0.2.0" 1015}])

(defn simplify-download-count [[[namespace name] version-map]]
  {:name (if (= namespace name) ;; if there's no namespace, the data we have repeats the library name in the namespace slot
           name
           (str namespace "/" name))
   :gross-downloads (apply + (vals version-map))})

;; Run the following to reproduce the results. Results are also stored in top_200_checked.edn.
;; (def things-to-check (take 200 (reverse (sort-by :gross-downloads (map simplify-download-count (clojure.edn/read-string (slurp "all-projects-from-data.edn"))))))) ;; Grab the list of projects by popularity (TODO: these maps doesn't include the project author, so the author sameness check in get-levenshtein-neighbors won't work).
;; (def all-projects (clojure.edn/read-string (slurp "mapped-projects-all.edn"))) ;; grab the scraped list of all projects
;; (time (spit "top_200_checked.edn" (vec (find-neighboring-projects things-to-check all-projects))))

