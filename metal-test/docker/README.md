# start service
```shell
dcp up -d
```
If the mongo database is never used, you should run the following script to init some collection.
```shell
mongoimport -c execs --file ../../build/sbin/db/execs.json --type json  mongodb://<credentials>@<db.mongo>:27017/metalDB
mongoimport -c metals --file ../../build/sbin/db/metals.json --type json  mongodb://<credentials>@<db.mongo>:27017/metalDB
mongoimport -c project --file ../../build/sbin/db/project.json --type json  mongodb://<credentials>@<db.mongo>:27017/metalDB
mongoimport -c user --file ../../build/sbin/db/user.json --type json  mongodb://<credentials>@<db.mongo>:27017/metalDB
```

