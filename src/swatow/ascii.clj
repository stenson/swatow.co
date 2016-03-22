(ns swatow.ascii
  (:require [clojure.string :as string]
            [mikera.image.core :as img])
  (:import (java.awt Color)))

(def phrase (atom ""))
(def pos (atom 0))

(defn get-text [length]
  (let [start @pos
        end (+ start length)]
    (reset! pos end)
    (subs @phrase start end)))

(defn group-colors [line]
  (->> line
       (reduce
         (fn [acc el]
           (let [l (last acc)]
             (if (= (first l) el)
               (concat (drop-last acc) [(flatten [l el])])
               (concat acc [[el]]))))
         [])
       (map (fn [g]
              (let [color (first g)]
                [:span
                 {:class (str "c-" (string/replace color #"," "-"))
                  :style (string/join
                           ";"
                           [(str "color:rgba(" color ",1.0)")
                            (str "background:rgba(" color ",0.1)")])}
                 (get-text (count g))])))))

(defn to-html [image-name scale phrase-string]
  (do
    (reset! phrase (string/join "" (repeat 10000 phrase-string)))
    (reset! pos 0)
    (let [image (-> (img/load-image image-name)
                    (img/scale scale))
          w (img/width image)]
      (->> (img/get-pixels image)
           (map #(new Color %))
           (map (fn [c]
                  [(.getRed c)
                   (.getGreen c)
                   (.getBlue c)]))
           (map (fn [[r g b]]
                  (string/join "," [r g b])))
           (partition w)
           (map group-colors)
           (map (fn [spans]
                  [:div.line spans]))))))