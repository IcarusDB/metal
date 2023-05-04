# start test service

```shell
dcp up -d
```

If the mongo database is never used, you should create `metalDB` database and run the following
script to import some collection into `metalDB`.

## create mongodb user

Please install `mongo-database-tools` before run script.
Run the following commands in mongo shell.

```shell
use metalDB
db.createUser({
  user: 'metal', 
  pwd: '123456', 
  roles: [role: 'root', db: 'admin']
})
```

## import collections

```shell
ls ../../build/sbin/db | awk -F '.' '{print $1}' | xargs -I {} mongoimport -c {} --type json --file ../../build/sbin/db/{}.json mongodb://<credentials>@<host>:<port>/metalDB
```

# start server

Before start metal server, some prepared tasks should be done.

- In the `build/conf` directory, `conf/metal-server.json` should be overrided according your
  envirnoment.
    - `mongoConf.connection_string` will be set according your product enviroment.
    - If your use Spark standalone
      cluster, `platform.spark.standalone.conf.sparkProperties.spark.driver.extraClassPath` will be
      set on the path of `build/conf/backend` in project service configure item.
    - In project service configure item, `backendJar`&`platform.spark.standalone.appResource`
      &`platform.spark.standalone.sparkProperties.spark.jars` will be set on the path
      of `metal-backend-{version}.jar`.

- In the `build/conf` directory, `zookeeper.json` should be overrided according your envirnoment.

```shell
java -cp ../../build/libs/metal-server-1.0.0-SNAPSHOT.jar:../../build/ui:../../build/conf org.metal.server.GatewayLauncher   
```

# Backend

When spark or the other platform submit a Backend Jar to run service:

- the `zookeeper.json` in `metal-backend-{version}.jar` should be override according your
  envirnoment. You can override `build/conf/backend/zookeeper` according your envirnoment.