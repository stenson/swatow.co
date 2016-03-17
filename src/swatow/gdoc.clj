(ns swatow.gdoc
  (:require [net.cgrand.enlive-html :as html]
            [hieronymus.core :as hiero]
            [clojure.string :as string]
            [clojure.set :as set]
            [swatow.pantry :as pantry])
  (:import (java.net URL)))

(defn g-dld [id fmt]
  (format (string/join
            "" ["https://docs.google.com/feeds/download/"
                "documents/export/Export?id=%s&exportFormat=%s"])
          id fmt))

(defn read-html [url-s]
  (html/html-resource (URL. url-s)))

(defn expand-css [css]
  (->> (string/split css #"\}")
       (map #(string/split % #"\{"))
       (map (fn [[sel rule-str]]
              [sel (string/split rule-str #";")]))
       (filter (fn [[sel]] (re-find #"\.c" sel)))))

(defn find-c-rules [rules]
  (let [find-style (fn [rule]
                     (first (first (filter (fn [[_ r]] (= rule r)) rules))))]
    {:strong (find-style ["font-weight:bold"])
     :em (find-style ["font-style:italic"])}))

(defn breakdown-span [{:keys [attrs content]}]
  (let [classes (into #{} (map keyword (string/split (:class attrs) #" ")))]
    {:classes classes
     :content (apply str content)}))

(defn p->md [p]
  (let [spans (map breakdown-span (html/select p [:span]))
        classes (map :classes spans)
        sentinel (first (set/difference
                          (apply set/union classes)
                          (apply set/intersection classes)))]
    (->> spans
         (map (fn [{:keys [classes content]}]
                (if (contains? classes sentinel)
                  (format "_%s_" content)
                  content)))
         (string/join ""))))

(defn html->hieronymus->html [h]
  (let [md (->> (html/select h [:p])
                (map p->md)
                (string/join "\n\n")
                (str " "))]
    (hiero/parse md {})))

(defn html-as-hieronymized-text [h]
  (->> (html/select h [:p])
       (map (fn [el]
              (let [t (html/text el)]
                (if (not (empty? t))
                  t
                  (when-let [img (first (html/select el [:img]))]
                    (let [{:keys [style src]} (:attrs img)]
                      (format "ƒ«img:%s»(ß:max-width:700px)" src)))))))
       (remove nil?)
       (string/join "\n\n")))

(defn parse
  ([style gid]
   (parse style gid true))
  ([style gid add-space?]
   (case style
     ;:html (html->hieronymus->html (read-html (g-dld gid "html")))
     :html (-> (g-dld gid "html")
               (read-html)
               (html-as-hieronymized-text)
               (hiero/parse {}))
     :txt (hiero/parse (str (if add-space? " ") (slurp (g-dld gid "txt"))) {}))))