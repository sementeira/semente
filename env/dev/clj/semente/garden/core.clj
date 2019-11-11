(ns semente.garden.core
  (:require
   [garden-watcher.core :as gwatcher]
   [garden-watcher.def :refer [defstyles]]
   [garden.units :refer [px]]
   [mount.core :as mount]
   [semente.style-constants :as style]))

(mount/defstate ^{:on-reload :noop} watcher
  :start
  (gwatcher/start-garden-watcher! '[semente.garden.core])
  :stop
  (gwatcher/stop-garden-watcher! watcher))

(def borde-externo (str "solid 1px " style/external-border-color))

(def horiz-padding "32px")

(defstyles semente
  [:#fondo-nacional {:background-color style/background-color-nacional}]
  [:header :nav :main :footer
   {:box-sizing :border-box
    :background-color :white
    :border-left borde-externo
    :border-right borde-externo
    :margin-top 0
    :margin-bottom 0
    :max-width (px 960)
    :margin-left :auto
    :margin-right :auto}]
  [:#logos {:padding (str "24px " horiz-padding)
            :display :flex
            :justify-content :space-between
            :align-items :flex-end
            :flex-wrap :wrap}]
  [:#contedor-social {:padding-left (px 24)
                      :padding-right (px 12)
                      :margin-top (px 12)}]
  [:a.social {:margin-left (px 2)
              :border-radius "4px"
              :padding-top (px 3)
              :padding-left (px 4)
              :padding-right (px 3)}
   [:&:active {:background-color style/semente-blue}]]
  [:a [:svg:hover [:path.socialpath {:fill style/semente-blue}]]]
  [:a [:svg:active [:path.socialpath {:fill :white}]]]
  [:header
   [:img {:padding-left (px 24)
          :padding-right (px 40)
          :max-width (px 400)}]
   [:.destacado {:margin (str "0px " horiz-padding)
                 :padding "24px 24px"
                 :color :white
                 :text-align :center
                 :font-size (px 48)
                 :font-weight :bold
                 :background-color style/semente-blue}]]
  [:nav
   {:padding "24px 0px"}
   [:ul {:background-color style/nav-grey
         :margin-top 0
         :margin-bottom 0
         :padding (str "12px " horiz-padding)
         :display :flex
         :justify-content :space-between}
    [:li {:list-style-type :none
          :color style/semente-blue}]]]
  [:h1:first-child {:margin-top 0}]
  [:a.social {:display :inline-block}]
  [:main {:padding (str "12px " horiz-padding)}]
  [:article {:overflow :hidden}])
