(ns rafavot.faver
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful])
  (:require [clojure.edn :as edn])
  (:import (twitter.callbacks.protocols SyncSingleCallback)))

(def last-tweet-id (atom nil))

(def config (edn/read-string (slurp "resources/config.edn")))

(def twitter-credentials
  (make-oauth-creds (:app-key config) (:app-secret config) (:user-token config) (:user-token-secret config)))

(defn print-api-error [response & [_]] (prn (get-twitter-error-message response)))

(def extra-params (if (nil? @last-tweet-id) {} {:since-id @last-tweet-id}))

(defn- timeline []
  (statuses-home-timeline :oauth-creds twitter-credentials :params (merge {:exclude-replies false} extra-params)))

(defn- search [query-search]
  (search-tweets :oauth-creds twitter-credentials :params (merge {:q query-search :result-type "recent"} extra-params)))

(defn- fav-tweet [tweet-id]
  (favorites-create :oauth-creds twitter-credentials
                    :callbacks (SyncSingleCallback.
                                 response-return-body
                                 print-api-error
                                 print-api-error)
                    :params {:id tweet-id})
  (prn (str "Tweet https://twitter.com/statuses/" tweet-id " faved")))

(defn- is-candidate-to-fav? [probability]
  (let [rand-num (inc (rand-int 100))]
    (< rand-num probability)))

(defn- try-to-fav-tweet [tweet probability whitelist]
  (if (and
        (false? (:favorited tweet))
        (is-candidate-to-fav? probability)
        (not-any? #(= (get-in tweet [:user :screen_name]) %) whitelist))
    (fav-tweet (:id_str tweet))))

(defn- try-to-fav-tweets [tweets probability whitelist]
  (doseq [tweet tweets] (try-to-fav-tweet tweet probability whitelist))
  (swap! last-tweet-id (fn [_] (:id_str (first tweets)))))

(defn fav-home-timeline [probability whitelist]
  (try-to-fav-tweets (:body (timeline)) probability whitelist))

(defn fav-search [query-search probability whitelist]
  (try-to-fav-tweets (get-in (search query-search) [:body :statuses]) probability whitelist))
