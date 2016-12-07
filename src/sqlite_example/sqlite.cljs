(ns sqlite-example.sqlite)

(def Sqlite (js/require "react-native-sqlite-storage"))

(comment
  (def db (.openDatabase Sqlite
                         "test.db"
                         "1.0"
                         "Test Database"
                         200000
                         (fn []
                           (prn "Database OPENED"))
                         (fn [err]
                           (prn "SQL Error: " err))))

  (.transaction db
                (fn [tx]
                  (.executeSql tx
                               "SELECT 1 FROM Version LIMIT 1"
                               []
                               (fn [tx results]
                                 (prn {:results results}))
                               )))

  (.transaction db
                (fn [tx]
                  (.executeSql tx "DROP TABLE IF EXISTS Users;")
                  (.executeSql tx "CREATE TABLE IF NOT EXISTS Users (user_id INTEGER PRIMARY KEY NOT NULL,
   name VARCHAR(64));" [])
                  (.executeSql tx "INSERT INTO Users (name) VALUES (\"Tim Duncan\")" [])
                  (.executeSql tx "INSERT INTO Users (name) VALUES (\"Manu Ginobili\")" [])
                  (.executeSql tx "INSERT INTO Users (name) VALUES (\"Tony Parker\")" [])
))

  (.transaction db
                (fn [tx]
                  (.executeSql tx
                               "Select * from Users"
                               []
                               (fn [tx results]
                                 (let [rows (aget results "rows")
                                       l (aget rows "length")]
                                   (dotimes [i l]
                                     (let [row (.item rows i)]
                                       (prn {:name (aget row "name")})))))
                               (fn [err]
                                 (prn "Query error: " err))))))
