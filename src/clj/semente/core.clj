;;; Compojure and Sente routing.

(ns semente.core
  (:gen-class)
  (:require [compojure.core :refer (defroutes GET POST)]
            [compojure.route :refer (files not-found resources)]
            [semente.sente :as sente]
            [semente.util :as util]
            [ring.middleware.defaults :refer (wrap-defaults site-defaults)]
            [rum.core :as rum]))

(def azul-semente "#009ca3")
(def verde-semente "#9eab05")

(def facebook-path
  "M22.675 0h-21.35c-.732 0-1.325.593-1.325 1.325v21.351c0 .731.593 1.324 1.325 1.324h11.495v-9.294h-3.128v-3.622h3.128v-2.671c0-3.1 1.893-4.788 4.659-4.788 1.325 0 2.463.099 2.795.143v3.24l-1.918.001c-1.504 0-1.795.715-1.795 1.763v2.313h3.587l-.467 3.622h-3.12v9.293h6.116c.73 0 1.323-.593 1.323-1.325v-21.35c0-.732-.593-1.325-1.325-1.325z")

(def twitter-path
  "M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z")

(def youtube-path
  "M4.652 0h1.44l.988 3.702.916-3.702h1.454l-1.665 5.505v3.757h-1.431v-3.757l-1.702-5.505zm6.594 2.373c-1.119 0-1.861.74-1.861 1.835v3.349c0 1.204.629 1.831 1.861 1.831 1.022 0 1.826-.683 1.826-1.831v-3.349c0-1.069-.797-1.835-1.826-1.835zm.531 5.127c0 .372-.19.646-.532.646-.351 0-.554-.287-.554-.646v-3.179c0-.374.172-.651.529-.651.39 0 .557.269.557.651v3.179zm4.729-5.07v5.186c-.155.194-.5.512-.747.512-.271 0-.338-.186-.338-.46v-5.238h-1.27v5.71c0 .675.206 1.22.887 1.22.384 0 .918-.2 1.468-.853v.754h1.27v-6.831h-1.27zm2.203 13.858c-.448 0-.541.315-.541.763v.659h1.069v-.66c.001-.44-.092-.762-.528-.762zm-4.703.04c-.084.043-.167.109-.25.198v4.055c.099.106.194.182.287.229.197.1.485.107.619-.067.07-.092.105-.241.105-.449v-3.359c0-.22-.043-.386-.129-.5-.147-.193-.42-.214-.632-.107zm4.827-5.195c-2.604-.177-11.066-.177-13.666 0-2.814.192-3.146 1.892-3.167 6.367.021 4.467.35 6.175 3.167 6.367 2.6.177 11.062.177 13.666 0 2.814-.192 3.146-1.893 3.167-6.367-.021-4.467-.35-6.175-3.167-6.367zm-12.324 10.686h-1.363v-7.54h-1.41v-1.28h4.182v1.28h-1.41v7.54zm4.846 0h-1.21v-.718c-.223.265-.455.467-.696.605-.652.374-1.547.365-1.547-.955v-5.438h1.209v4.988c0 .262.063.438.322.438.236  0 .564-.303.711-.487v-4.939h1.21v6.506zm4.657-1.348c0 .805-.301 1.431-1.106 1.431-.443 0-.812-.162-1.149-.583v.5h-1.221v-8.82h1.221v2.84c.273-.333.644-.608 1.076-.608.886 0 1.18.749 1.18 1.631v3.609zm4.471-1.752h-2.314v1.228c0 .488.042.91.528.91.511 0 .541-.344.541-.91v-.452h1.245v.489c0 1.253-.538 2.013-1.813 2.013-1.155 0-1.746-.842-1.746-2.013v-2.921c0-1.129.746-1.914 1.837-1.914 1.161 0 1.721.738 1.721 1.914v1.656z")

(defn root [req]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html
          [:html
           [:head [:title "Escola de Ensino Galego Semente"]
            ;;[:link {:rel :stylesheet :href "https://fonts.googleapis.com/css?family=Ubuntu"}]
            [:link {:rel :stylesheet :href "css/semente.css"}]]
           [:body {:style {:display :flex
                           :padding "1em"
                           :flex-direction :column
                           :align-items :center}}
            [:div {:style {:flex "1 1 960px"
                           :width "100%"
                           :max-width "960px"}}
             [:header {:style  {:display :flex
                                :justify-content :center
                                :align-items :flex-end}}
              [:img {:src "img/logo-nacional.svg"
                     :style {:max-width "100%"
                             :max-height "100%"}}]]
             [:nav
              [:div {:style {:color :white
                             :background-color azul-semente
                             :display :flex
                             :justify-content :space-around
                             :align-items :stretch
                             :flex-wrap :wrap}}
               [:div.navitem.ondetou "Quem Somos"]
               [:a.navitem {:href "#ppios"} "Princípios"]
               [:a.navitem {:href "#hist"} "História"]
               [:a.navitem {:href "#rec"} "Recursos"]
               ;[:div.ondetou "Recursos"]
               [:a.navitem {:href "#loja"} "Loja Online"]
               [:a.navitem {:href "#cont"} "Contato"]
               [:a.navitem {:href "#assoc"} "Associa-te"]]
              [:div {:style {:display :flex
                             :flex-wrap :wrap
                             :justify-content :space-between}}
               [:div]
               [:div {:style {:display :flex
                              :flex-wrap :wrap}}
                [:a.social {:href "#twitter"}
                 [:svg.socialsvg {:viewBox "0 0 24 24"}
                  [:path.socialpath {:fill azul-semente
                                     :d twitter-path}]]]
                [:a.social {:href "#facebook"}
                 [:svg.socialsvg {:viewBox "0 0 24 24"}
                  [:path.socialpath {:fill azul-semente
                          :d facebook-path}]]]
                [:a.social {:href "#youtube"}
                 [:svg.socialsvg {:viewBox "0 0 24 24"}
                  [:path.socialpath {:fill azul-semente
                          :d youtube-path}]]]]]]
             [:main "Conteúdo"
              [:article "Artigo1"]
              [:article "Artigo2"]
              [:aside "Porsierto..."]]
             [:footer
              [:img {:src "img/ramalho.svg"}]]
             [:div#app_container "Wonderful things would happen here if you had Javascript enabled..."]]]
           [:script {:type "text/javascript" :src "js/main.js"}]
           [:script {:type "text/javascript"} "semente.core.main();"]])})

(defroutes handler
  (GET "/" req (root req))
  ;; sente
  (GET  "/chsk" req sente/ring-ajax-get-or-ws-handshake)
  (POST "/chsk" req sente/ring-ajax-post)
  (resources (if util/in-development? "/public" "/"))
  (files "/")
  (not-found "Page not found."))

(def app
  (wrap-defaults handler site-defaults))

(if util/in-development?
  (sente/start-router!))

(defn -main []
  (sente/start-router!))
