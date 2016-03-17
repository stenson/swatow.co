(ns swatow.html
  (:require [garden.core :as garden]
            [hiccup.page :refer [html5]]
            [me.raynes.fs :as fs]))

(defn favicon [size]
  [:link {:href  (format "favicon-%sx%s.png" size size)
          :sizes (format "%sx%s" size size)
          :rel   "icon"
          :type  "image/png"}])

(defn now []
  (quot (System/currentTimeMillis) 1000))

(defn favicons [& sizes]
  (->> (map favicon sizes)
       (conj [:link {:href "favicon.ico" :rel "icon" :type "image/x-icon"}])))

(defn inline-style [& styles]
  [:style {:type "text/css"}
   (garden/css {:pretty-print? false} styles)])

(defn style-link [href]
  [:link {:type "text/css"
          :href (format "%s.css?n=%s" href (now))
          :rel  "stylesheet"}])

(defn js-link [href]
  [:script {:type "text/javascript"
            :src  (format "%s.js?n=%s" href (now))}])

(defn inline-js [src]
  [:script {:type "text/javascript"} src])

(defn basic [title {:keys [styles scripts favicons]} content]
  (html5
    {:lang "en"}
    (list
      [:head
       [:meta {:charset "utf-8"}]
       [:title title]
       (when favicons
         (list (favicons 16 32 96)))
       (if (string? styles)
         (format [:style {:type "text/css"} styles])
         (list (map style-link styles)))]
      [:body
       (list
         content
         (list (map js-link scripts)))])))

(defn refresh [site title options content]
  (let [folder (format "sites/%s" site)]
    (fs/mkdirs folder)
    (spit
      (str folder "/index.html")
      (basic title options content))))