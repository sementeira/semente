(ns semente.core
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as wflows]
            [cemerick.friend.credentials :as creds]
            [clojure.string :as str]
            [compojure.core :refer (defroutes GET POST)]
            [compojure.route :refer (resources not-found)]
            [datomic.ion.lambda.api-gateway :as apigw]
            [net.icbink.expand-headers.core :refer (wrap-expand-headers)]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.session :refer (wrap-session)]
            [ring.middleware.session.cookie :refer (cookie-store)]
            [rum.core :as rum]
            [semente.elasticsearch :as es]
            [clojure.data.json :as json]))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "user_password")
                    :roles #{::user}}})

(rum/defc login-form []
  [:html
   [:body
    [:form {:action "/login" :method "post"}
     [:div
      [:div "utente"]
      [:input {:type "text" :name "username"}]]
     [:div
      [:div "senha"]
      [:input {:type "password" :name "password"}]]
     [:div
      [:input {:type "submit"}]]]]])

(rum/defc edit [doc-name contents]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:link {:rel "stylesheet" :type "text/css" :href "/css/Draft.css"}]
    [:link {:rel "stylesheet" :type "text/css" :href "/css/prova.css"}]]
   [:body
    [:#app "Aqui iriam as tuas movidorras."]
    [:script {:src "/cljs-out/dev-main.js" :type "text/javascript"}]
    [:script {:type "text/javascript"
              :dangerouslySetInnerHTML {:__html
                                        (str "semente.webmain.main("
                                             (pr-str doc-name)
                                             ", "
                                             (or contents "null")
                                             ");")}}]]])

(defn merge-style [start end style]
  (fn [xf]
    (fn
      ([] (xf))
      ([result] (xf result))
      ([result [input-start input-end input-styles]]
       (let [xf-maybe
             (fn [result start end styles]
               (if (< start end)
                 (xf result [start end styles])
                 result))]
         (-> result
             (xf-maybe input-start
                       (min start input-end)
                       input-styles)
             (xf-maybe (max start input-start)
                       (min end input-end)
                       (conj input-styles style))
             (xf-maybe (max end input-start)
                       input-end
                       input-styles)))))))

(def block-style->tag
  {"BOLD" :strong
   "ITALIC" :em})

(defn add-span [spans next]
  (let [start (:offset next)
        end (+ start (:length next))
        style (-> next :style block-style->tag)]
    (into [] (merge-style start end style) spans)))

(comment
  (into [] (merge-style 0 40 :bold) spans)
  (def block
    {:key "ds774", :text "mola! e prova deveria ser algo que tenha", :type "unstyled", :depth 0, :inlineStyleRanges [{:offset 35, :length 5, :style "ITALIC"}], :entityRanges [], :data {}})
  (add-span [[0 (.length (:text block)) []]] (first (:inlineStyleRanges block)))
  (def spans [[0 (.length (:text block)) []]])
  (def next *1)
  (def start (:offset next))
  (def end (+ start (:length next)))
  (def style *1)
  (def ms *1)
  (render-text block)
  )

(defn render-text [{:keys [text] :as block}]
  (println "block si")
  (prn block)
  (println)
  (let [spans (reduce add-span
                      [[0 (.length text) []]]
                      (:inlineStyleRanges block))]
    (for [[start end styles] spans]
      (reduce (fn [x style] [style x]) (subs text start end) styles))))

(defn content-state->hiccup [content-state]
  (println "content state si")
  (prn content-state)
  (println)
  (for [b (:blocks content-state)]
    (if (= (:type b) "unstyled")
      [:p {:key (:key b)} (render-text b)]
      [:pre {:key (:key b)} (with-out-str (clojure.pprint/pprint b))])))

(rum/defc view [contents]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]]
   [:body (content-state->hiccup contents)]])

(defroutes ring-handler
  (POST "/guarda" [name contents & etc]
        (do
          (println "got etc:")
          (clojure.pprint/pprint etc)
          (es/save-doc name {:contents contents})))
  (GET "/prova" [] "Olá!")
  (GET "/edit/:id" [id] (rum/render-static-markup (edit id (get (es/load-doc id) "contents"))))
  (GET "/view/:id" [id] (some-> id
                                es/load-doc
                                (get "contents")
                                (json/read-str :key-fn keyword)
                                view
                                rum/render-static-markup))
  (resources "/")
  (not-found "U-lo?"))

(comment
  ;; auth stuff
  (and (= request-method :get) (= uri "/login"))
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (rum/render-static-markup (login-form))}
  (friend/authorized? #{::admin ::user} friend/*identity*)
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Olá amo! 3"}
  :else
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (str "Olá plebeio! 7" uri)}
  )

(def ring-app
  (-> ring-handler
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
                            :workflows [(wflows/interactive-form)]})
      (wrap-session {:store (cookie-store {:key "a 16-byte secret"})})
      wrap-keyword-params
      wrap-params
      wrap-expand-headers))

(def webroot (apigw/ionize ring-app))
