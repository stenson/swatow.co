(ns swatow.pijiu
  (:require [swatow.ascii :as ascii]
            [swatow.html :as html]
            [garden.core :as garden]
            [swatow.pijiu.posts :as posts]))

(defn page [slug title subheader banner html]
  (html/refresh
    (str "lager.robstenson.com" slug)
    (str title (if title " — ") "League of Lagers")
    {:favicons true
     :scripts ["/hyphenator"]
     :styles ["/styles"
              "/fonts/fonts"
              "https://fonts.googleapis.com/css?family=UnifrakturCook:700"]}
    [:div#content
     [:pre#banner
      {:style (garden/style {:font-size "7px"
                             :line-height "4px"
                             :text-align "center"
                             :background "white"})}
      banner]
     [:div#header
      (if (nil? title)
        "League of Lagers"
        [:a {:href "/"} "League of Lagers"])]
     [:div#subheader
      subheader]
     #_(if (nil? title)
       [:div#sponsorship
        "Brought you to by "
        [:a {:href "/" :style "color:red"} "swatow.co"]])
     html]))

(page
  nil
  nil
  "Histories of the beers of the world"
  (:art posts/intro)
  (list
    [:div#toc
     (->> [posts/intro
           posts/victoria
           posts/tsingtao
           posts/tusker
           posts/orion
           posts/skøl]
          (map-indexed
            (fn [idx {:keys [title blurb slug]}]
              [:div.chapter
               [:div.num idx]
               (if slug
                 [:a {:href slug} title]
                 [:span title])
               (when blurb
                 [:div.blurb blurb])])))]
    [:div.nb
     [:em "Unlinked posts are still being written. To be notified of posts as they become available, simply"]
     [:form#subscribe
      {:action "https://tinyletter.com/lager"
       :method "post"
       :target "popupwindow"
       :onsubmit "window.open('https://tinyletter.com/lager', 'popupwindow', 'scrollbars=yes,width=800,height=600');return true"}
      [:p [:label {:for "tlemail"} "Enter your email address"]]
      [:p [:input#tlemail {:type "text", :style "width:140px", :name "email"}]]
      [:input {:type "hidden", :value "1", :name "embed"}]
      [:input {:type "submit", :value "Subscribe"}]]]))
(page
  "/introduction"
  "Introduction"
  (:title posts/intro)
  (:art posts/intro)
  [:div#posts (:html posts/intro)])