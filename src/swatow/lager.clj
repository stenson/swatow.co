(ns swatow.lager
  (:require [swatow.ascii :as ascii]
            [swatow.html :as html]))

(html/refresh
  "lager.swatow.co"
  "League of Lagers"
  {:favicons false
   :styles ["styles"
            "https://fonts.googleapis.com/css?family=UnifrakturCook:700"]}
  [:div#content
   [:pre#banner
    (ascii/to-html
      "assets/images/pijiu.png"
      0.2
      "LEAGUE•OF•LAGERS‡")]
   [:div#header
    "League of Lagers"]])