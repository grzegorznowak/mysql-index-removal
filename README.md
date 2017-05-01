# mysql-index-removal

A Clojure tooling script that helps to brute-remove mysql indexes from a db.
Written mainly for Clojure practice and a specific immediate need's purposes.

Sample CLI usage:

`lein run -u [user] -p [password] -d [database-name]`

It will attempt to remove foreing keys too, which will usually result in "couldn't remove the key" message, so be warned.
