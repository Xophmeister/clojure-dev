(ns tasks
  (:require
   [clojure.edn]
   [clojure.java.io]
   [clojure.pprint]
   [clojure.set :as set]
   [clojure.string :as str]))

;; Data model

(defn create-task
  "Create a task, with optional tags"
  [title & tags]

  {:id         (str (random-uuid))
   :title      title
   :status     :todo
   :created-at (.toString (java.time.Instant/now))
   :tags       (into #{} tags)})

(defn progress-task
  "Progress the status of the given task"
  [task]

  (case (:status task)
    ; To do -> doing
    :todo (assoc task :status :doing)

    ; Doing -> done
    :doing (assoc task :status :done)

    ; Do nothing
    task))

(defn regress-task
  "Regress the status of the given task"
  [task]

  (case (:status task)
    ; Done -> doing
    :done (assoc task :status :doing)

    ; Doing -> to do
    :doing (assoc task :status :todo)

    ; Do nothing
    task))

;; IO

(def tasks-edn
  "Convenience constant for ~/.tasks.edn"

  (let [home (System/getProperty "user.home")]
    (str home "/.tasks.edn")))

(defn read-tasks
  "Read tasks from disk, defaulting to an empty vector"
  []

  (try
    (clojure.edn/read-string (slurp tasks-edn))
    (catch java.io.FileNotFoundException _ [])))

(defn write-tasks
  "Write tasks to disk"
  [& tasks]

  (let [v (into [] tasks)]
    (with-open [w (clojure.java.io/writer tasks-edn)]
      (binding [*out* w]
        (clojure.pprint/pprint v)))))

;; Miscellaneous helpers

(defn arg->keyword [s]
  (keyword (if (str/starts-with? s ":") (subs s 1) s)))

(defn task-by-id?
  "Check whether task matches the given ID substring"
  [id task]

  (str/starts-with? (:id task) id))

(defn print-tasks [& tasks]
  (doseq [{:keys [id title status created-at tags]} tasks]
    (println (format "Task\t%s"    title))
    (println (format "ID\t%s"      id))
    (println (format "Status\t%s"  status))
    (println (format "Tags\t%s"    (str/join " " (map str tags))))
    (println (format "Created\t%s" created-at))
    (println)))

;; Task vector commands

(defn add-cmd
  "Add a task"
  [tasks title & tags]
  {:pre [(vector? tasks)
         (string? title)
         (every? #(keyword? (keyword %)) tags)]}

  (let [new-task (apply create-task title (map arg->keyword tags))]
    (println "New task:" (str (:id new-task)))
    (conj tasks new-task)))

(defn list-cmd
  "List tasks"

  ; Incomplete tasks
  ([tasks]
   {:pre [(vector? tasks)]}

   (apply print-tasks (filter #(not= (:status %) :done) tasks)))

  ; Tagged tasks
  ([tasks & tags]
   {:pre [(vector? tasks)
          (every? #(keyword? (keyword %)) tags)]}

   (let [all?      (= (first tags) "--all")
         needle    (into #{} (map arg->keyword tags))
         is-task?  #(or all?
                        (set/subset? needle (:tags %)))]

     (apply print-tasks (filter is-task? tasks)))))

(defn xgress-cmd
  "Progress/regress task status"
  [xgress tasks id]
  {:pre [(fn?     xgress)
         (vector? tasks)
         (string? id)]}

  (let [is-task? (partial task-by-id? id)]
    (mapv #((if (is-task? %) xgress identity) %) tasks)))

(defn usage-cmd
  "Usage"
  [subcommand & _]

  (println "Unknown command:" (or subcommand "<missing>"))
  (println)
  (println "Usage: tasks.clj <command> [args]")
  (println "- add <title> [:tags...]  Add a task")
  (println "- list [--all|:tags...]   List incomplete/all/tagged tasks")
  (println "- progress <id>           Progress task status")
  (println "- regress <id>            Regress task status"))

;; CLI entrypoint

; NOTE We roll our own CLI argument parser, rather than say
; clojure.tools.cli/parse-opts, for simplicity's sake
(defn -main [& args]
  (let [[subcommand & subargs] args

        ; Dispatch helpers
        runner  #(partial % (read-tasks))
        runner! #(comp (partial apply write-tasks) (runner %))]

    (apply
     (case subcommand
       "add"      (runner! add-cmd)
       "list"     (runner  list-cmd)
       "progress" (runner! (partial xgress-cmd progress-task))
       "regress"  (runner! (partial xgress-cmd regress-task))

       (partial usage-cmd subcommand))

     subargs)))

(apply -main *command-line-args*)
