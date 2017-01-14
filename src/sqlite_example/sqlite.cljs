(ns sqlite-example.sqlite
  (:require [cljs-exponent.core :refer [exponent]]))

(def Sqlite (aget exponent "Sqlite"))

;; openDatabase
;; (in DOMString name, in DOMString version, in DOMString displayName, in unsigned long estimatedSize, in optional DatabaseCallback creationCallback)
(def db (.openDatabase Sqlite
                       "test.db"
                       "1.0"
                       "Test Database"
                       200000
                       (fn []
                         (prn "Database OPENED"))
                       (fn [err]
                         (prn "SQL Error: " err))))

;; transaction + executeSql
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

;; readTransaction
(.readTransaction db
                  (fn [tx]
                    (.executeSql tx "INSERT INTO Users (name) VALUES (\"Kobe Byrant\")" []))
                  ;; prn
                  ;; a statement with no error handler failed: invalid sql for a read-only transaction
                  ;; TODO why no error handler failed?
                  (fn [err]
                    (prn {:err (aget err "message")}))
                  (fn [suc]
                    (prn "read transaction success, no!!!")))

;; sqlBatch insert,
;; read don't support ([tx result] (handler)) callback.
(.sqlBatch db
           (clj->js
            [["INSERT INTO Users (name) VALUES (\"name1\")" []]
             ["INSERT INTO Users (name) VALUES (\"name2\")" []]
             ["INSERT INTO Users (name) VALUES (\"name3\")" []]])
           (fn [suc]
             (prn "sqlBatch insert successfully!"))

           (fn [err]
             (prn "sqlBatch insert failed: " err)))

(.readTransaction db
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
                                   (prn "Query error: " err)))))

;; attach
;; (.attach db "second" "second"
;;          (fn [suc]
;;            (prn "attach successfully!"))
;;          (fn [err]
;;            (prn "attach failed: " err)))

;; detach


(comment
  ;; close
  (.close db
          (fn [suc]
            (prn "close db successfully!"))
          (fn [err]
            (prn "close db error: " err)))

  ;; deteleDatabase
  (.deleteDatabase Sqlite
                   "test.db"
                   (fn [suc]
                     (prn "Delete db successfully!"))

                   (fn [err]
                     (prn "Delete db failed: " err)))

  )
