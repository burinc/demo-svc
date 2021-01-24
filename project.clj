(defproject net.b12n.demo-svc/demo-svc-cli "1.0.0"
  :description
  "Simple REST API Demo"

  :url
  "http://github.com/burinc/demo-svc"

  :license
  {:name "Eclipse Public License"
   :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :bin {:name     "srk"
        :bin-path "."}

  :plugins
  [[lein-ancient    "0.7.0"]
   [lein-binplus    "0.6.6"]
   [lein-cljfmt     "0.7.0"]
   [lein-cloverage  "1.2.2"]
   [lein-tools-deps "0.4.5"]]

  ;; Extra middleware to make use of deps.clj from project.clj
  :middleware
  [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]

  ;; The default project will include :deps along with :extra-deps
  ;; defined with the :async alias.
  :lein-tools-deps/config
  {:config-files
   [:install :user :project]
   :resolve-aliases [:depstar]}

  ;; NOTE: we target the cli as the default main entry point for lein-binplus
  :main net.b12n.demo-svc.core
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[ring/ring-mock "0.4.0"]]}})
