(ns swatow.pijiu
  (:require [swatow.ascii :as ascii]
            [swatow.html :as html]
            [garden.core :as garden]
            [swatow.pijiu.posts :as posts]))

(defn page [slug title subheader banner html]
  (html/refresh
    (str "pijiu.swatow.co" slug)
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
    [:ol#toc {:start "0"}
     [:li [:a {:href "/introduction"} "Introduction"]]
     [:li [:span "Gambrinus, Emperor of Mexico"]]
     [:li [:span "The Many Owners of Tsingtao"]]
     [:li [:span "A Beer Named for its Brewer’s Death"]]
     [:li [:span "Orion’s Three Stars"]]
     [:li [:span "Skøl & the End of Beer"]]]
    [:div.nb
     [:em "Unlinked posts are still being written..."]]))
(page
  "/introduction"
  "Introduction"
  (:title posts/intro)
  (:art posts/intro)
  [:div#posts (:html posts/intro)])