(ns semente.auth
  (:require [datomic.client.api :as d]
            ring.util.response
            [rum.core :as rum]
            [semente.datomic :refer (conn)]))

(def login-uri "/entra")

(def init-permission-hierarchy
  (memoize
   (fn [p]
     (let [m (d/pull (d/db (conn))
                     '[{:permission/privilege [:db/ident]}
                       {:permission/scope [:db/ident]}]
                     p)]
       (derive p :permission/any)
       (some->> (get-in m [:permission/privilege :db/ident]) (derive p))
       (some->> (get-in m [:permission/scope :db/ident]) (derive p))))))

(defn init-permission-hierarchies [ps]
  (dorun (map init-permission-hierarchy ps)))

(defn load-credentials [username]
  (let [m (d/pull (d/db (conn))
                  '[:user/password-hash {:user/permission [:db/ident]}]
                  [:user/name username])]
    (when (not-empty m)
      {:username username
       :password (:user/password-hash m)
       :roles (doto (into #{} (map :db/ident (:user/permission m)))
                init-permission-hierarchies)})))

(defn- username
  [form-params params]
  (or (get form-params "username") (:username params "")))

(defn unauthenticated-handler
  [{:keys [form-params params]}]
  (ring.util.response/redirect
   (str login-uri "?erro=nom-quadra&utente="
        (java.net.URLEncoder/encode (username form-params params)))))

(defn unauthorized-handler [{{:keys [cemerick.friend/required-roles]} :cemerick.friend/authorization-failure
                             :as req}]
  (let [required-permission-names
        (map first
             (d/q '[:find ?n
                    :in $ [?r ...]
                    :where [?r :permission/display-name ?n]]
                  (d/db (conn))
                  required-roles))]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (rum/render-static-markup
            [:html
             [:head
              [:meta {:char-set "UTF-8"}]
              [:title "Erro de autorizaçom"]]
             [:body
              [:h1 "Erro de autorizaçom"]
              (if (= (count required-permission-names) 1)
                [:div "Esta página requer o permisso "
                 [:strong (first required-permission-names)]
                 "."]
                [:div "Esta página requer os permissos:"
                 [:ul
                  (for [p required-permission-names]
                    [:li [:strong p]])]])]])}))

(rum/defc login-form [erro utente]
  [:html
   [:head [:meta {:charset "UTF-8"}]]
   [:body
    [:h1 "Quem és?"]
    (if erro
      ;; XXX: estilo
      [:div.erro {:style {:color :red}} "Nom conheço " [:em utente] " ou a senha nom quadra."]
      [:div "Esta página requer autenticaçom."])
    [:form {:action login-uri :method "post"}
     [:div
      [:div "utente"]
      [:input {:type "text" :name "username" :value utente}]]
     [:div
      [:div "senha"]
      [:input {:type "password" :name "password"}]]
     [:div
      [:input {:type "submit"}]]]]])

(comment
  ;; to set a password:
  (d/transact (conn) {:tx-data [[:db/add [:user/name "estevo"] :user/password-hash (creds/hash-bcrypt "abcd")]]})
  )

(defn login [erro utente]
  (rum/render-static-markup (login-form erro utente)))