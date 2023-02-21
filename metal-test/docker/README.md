# start service
```shell
dcp up -d
```
If the mongo database is never used, you should run the following script to init some collection.
```shell
ls ../../build/sbin/db | awk -F '.' '{print $1}' | xargs -I {} mongoimport -c {} --type json --file ../../build/sbin/db/{}.json mongodb://<credentials>@<host>:<port>/metalDB
```

