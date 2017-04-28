(ns mysql-index-removal.core
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.tools.cli :refer [parse-opts]]))

(defn table-names
  [db]
  (sql/query db  ["show tables"]))

(defn table-indexes
  [db table-name]
  (sql/query db [(str "SHOW INDEX FROM " table-name)]))

(defn primary?
  [index]
  (= (:key_name index) "PRIMARY"))

(defn drop-the-index
  [db index]
  (let [table-name (:table index)
        index-name (:key_name index)]
    (println "dropping index" index-name "for the table" table-name)
    (try
      (sql/execute! db [(str "ALTER TABLE " table-name " DROP INDEX " index-name)])
      (catch Exception _
        (println "couldn't drop the index, sorry!")))))

(defn drop-non-primary-indexes
  [db indexes]
  (map
    (fn [i] (doall (map (partial drop-the-index db) (remove primary? i))))
    indexes))

(def cli-options
  [["-u" "--user VALUE"     "mysql usernam"       :default nil]
   ["-p" "--password VALUE" "mysql user password" :default nil]
   ["-d" "--database VALUE" "database name"       :default nil]])

(defn -main [& args]
  (let [parsed-options (:options (parse-opts args cli-options))
        user-name      (:user parsed-options)
        user-password  (:password parsed-options)
        db-name        (:database parsed-options)
        db-connection  {:classname "com.mysql.jdbc.Driver"
                        :subprotocol "mysql"
                        :subname (str "//localhost:3306/" db-name)
                        :user user-name
                        :password user-password}
        table-names    (table-names db-connection)
        indexes        (map (partial table-indexes db-connection) (map (fn [a] (get a (first (keys a)))) table-names))] ;; unwrap from ArrayMap then pass to the final map\
     (doall (drop-non-primary-indexes db-connection indexes))))