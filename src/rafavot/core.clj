(ns rafavot.core
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [chime :refer [chime-ch]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.core :as t]
            [rafavot.faver :as faver]
            [clojure.core.async :as a :refer [<! go-loop]])
  (:gen-class))

(def cli-options
  [["-w" "--whitelist USERS" "Comma separated twitter usernames"
    :default []
    :parse-fn #(string/split % #",")]
   ["-p" "--probability PROBABILITY" "Probability of a tweet to be faved"
    :default 50
    :parse-fn #(Integer/parseInt %)]
   ["-e" "--every MINUTES" "Every the tweets will be faved in minutes"
    :default 15
    :parse-fn #(Integer/parseInt %)]
   ["-t" "--type TYPE" "Type to Fav, timeline or search"
    :default "timeline"]
   ["-s" "--search QUERY" "If it's timeline type, the query to search"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        every (periodic-seq (t/now) (-> (:every options) t/minutes))
        schedule (chime-ch every)]
    (a/<!!
      (go-loop []
        (when-let [job-time (<! schedule)]
          (prn "Starting fav at: " (.toString job-time))
          (if (= "timeline" (:type options))
            (faver/fav-home-timeline (:probability options) (:whitelist options))
            (faver/fav-search (:search options) (:probability options) (:whitelist options)))
          (prn "Done!")
          (prn "-------------------------------------------------------")
          (recur))))))
