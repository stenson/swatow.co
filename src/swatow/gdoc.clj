(ns swatow.gdoc
  (:require [net.cgrand.enlive-html :as html]
            [hieronymus.core :as hiero]
            [clojure.string :as string]
            [clojure.set :as set]
            [swatow.pantry :as pantry]
            [me.raynes.cegdown :as md]
            [hiccup-bridge.core :as hicv])
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
                     (first (first (filter (fn [[_ r]] (= rule r)) rules))))
        strong (find-style ["font-weight:bold"])
        em (find-style ["font-style:italic"])]
    {strong #(str "__" % "__")
     em #(str "_" % "_")}))

(defn breakdown-span [{:keys [attrs content]}]
  (let [classes (into #{} (map keyword (string/split (:class attrs) #" ")))]
    {:classes classes
     :content (apply str content)}))

(defn text->hiccup [h]
  (let [css (-> (html/select h [:head :style])
                (first)
                (:content)
                (first)
                (expand-css)
                (find-c-rules))]
    (->> (html/select h [:p])
         ;(map :content)
         ;(remove #(string/blank? (string/join "" (map html/text %))))
         (map (fn [p]
                (->> (html/select p [:span])
                     (map breakdown-span)
                     (map (fn [{:keys [classes content]}]
                            (if-let [f (->> classes
                                            (map #(str "." (name %)))
                                            (map #(get css %))
                                            (remove nil?)
                                            (first))]
                              (f content)
                              content)))
                     (string/join ""))))
         (remove string/blank?)
         ;(partition-by #(= "+++"))
         (partition-by #(= "+++" %))
         (first)
         (string/join "\n\n")
         (md/to-html)
         (hicv/html->hiccup)
         (first)
         (second))))

(def f " <form style=\"border:1px solid #ccc;padding:3px;text-align:center;\" action=\"https://tinyletter.com/lager\" method=\"post\" target=\"popupwindow\" onsubmit=\"window.open('https://tinyletter.com/lager', 'popupwindow', 'scrollbars=yes,width=800,height=600');return true\"><p><label for=\"tlemail\">Enter your email address</label></p><p><input type=\"text\" style=\"width:140px\" name=\"email\" id=\"tlemail\" /></p><input type=\"hidden\" value=\"1\" name=\"embed\"/><input type=\"submit\" value=\"Subscribe\" /><p><a href=\"https://tinyletter.com\" target=\"_blank\">powered by TinyLetter</a></p></form>\n         ")

(defn gdoc->hiccup [gid]
  (let [hiccup (->> (g-dld gid "html")
                    (read-html)
                    (text->hiccup))]
    (vec (cons :div.post (vec (rest hiccup))))))

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