(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"res"}
 :dependencies '[[org.apache.httpcomponents/httpclient "4.5.5"]
                 [adzerk/boot-cljs "2.1.4" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [amazonica "0.3.119"]
                 ;; We don't use 1.3.2-1 because of
                 ;; https://github.com/martinklepsch/boot-garden/issues/25
                 [org.martinklepsch/boot-garden "1.3.2-0"]
                 [adzerk/boot-reload "0.5.2" :scope "test"]
                 [pandeiro/boot-http "0.8.3" :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [boot-environ "1.0.3"]
                 [com.cemerick/friend "0.2.3"]
                 [clj-time "0.13.0"]
                 [cljsjs/quill "1.2.5-4"]
                 ;; using the alpha because that's the version of the API docs
                 ;; in their website.
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 [digest "1.4.6"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [compojure "1.4.0"]
                 [org.clojure/core.async "0.3.465"]
                 [datascript "0.15.2"]
                 [com.datomic/datomic-free "0.9.5544" :exclusions [com.google.guava/guava]]
                 [environ "1.0.3"]
                 [garden "1.3.3"]
                 [http-kit "2.1.19"]  ;; same as used by boot-http
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [ring/ring-defaults "0.1.5"]
                 [rum "0.10.8"]
                 [com.taoensso/sente "1.12.0"]
                 [com.taoensso/timbre "4.7.2"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [weasel "0.7.0" :scope "test"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[org.martinklepsch.boot-garden :refer [garden]]
  '[adzerk.boot-reload    :refer [reload]]
  '[environ.boot :refer [environ]]
  '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask auto-test []
  (merge-env! :resource-paths #{"test"})
  (comp (watch)
     (speak)
     (test-cljs)))

(deftask dev []
  (comp (environ :env {:in-development "indeed"})
     (serve :handler 'semente.core/app
            :resource-root "target"
            :httpkit true
            :reload true)
     (watch)
     (speak)
     (garden :styles-var 'semente.styles/base
             :pretty-print true
             :output-to "public/css/garden.css")
     (reload ;; XXX: make this configurable
             :open-file "emacsclient -n +%s:%s %s")
     (cljs-repl)
     (cljs :source-map true :optimizations :none)
     (target :dir #{"target"})))

(deftask build []
  (comp
   (cljs :optimizations :advanced)
   (aot :namespace '#{semente.core})
   (pom :project 'semente
        :version "0.1.0-SNAPSHOT")
   (uber)
   (jar :main 'semente.core)
   (target :dir #{"target"})))