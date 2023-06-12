# Quick Start

<img src="/metal-ui/public/images/metal_logo.svg" alt="metal logo" height="200px" align="right" />

---

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](/doc/Quick_Start.md) [![CN doc](https://img.shields.io/badge/document-Chinese-yellow.svg)](/doc/Quick_Start_cn.md)

---

In order to experience Metal quickly, you can create a test environment through the following installation steps.

## Pre-Prepared

- OpenJDK 11 and above
- npm 9.5.0 and above
- Docker
- docker-compose
- maven
- mongodb tools

## Compile & Package

Under the root directory of the Metal project, execute the following command to compile and package.

```shell
mvn package -Dmaven.test.skip=true
```

The packaged content is saved in the `./build` directory.

```shell
./build/
├── conf
│   ├── backend
│   │   ├── META-INF
│   │   ├── log4j.properties
│   │   └── zookeeper.json
│   ├── conf
│   │   └── metal-server.json
│   ├── log4j.properties
│   ├── metal-server.openapi.json
│   └── zookeeper.json
├── libs
│   ├── metal-backend-{VERSION}.jar
│   └── metal-server-{VERSION}.jar
├── sbin
│   └── db
│       ├── execs.json
│       ├── metals.json
│       ├── project.json
│       └── user.json
└── ui
    └── webroot
        ├── asset-manifest.json
        ├── favicon.ico
        ├── images
        ├── index.html
        ├── logo192.png
        ├── logo512.png
        ├── manifest.json
        ├── robots.txt
        └── static
```

## Running Environment

### Start Service

Enter the `./metal-test/docker` directory under the project root directory, and start related dependent services (including MongoDB, MongoDB Express, Zookeeper, Spark Standalone cluster and HDFS) through the following command.

```shell
docker-compose up -d
```

Of course, if you don't plan to perform any tasks and just want to experience the effect of Metal, you can only start MongoDB and Zookeeper. Just execute the following command.

```shell
docker-compose up -d mongo zoo0
```

### Initialize MongoDB

First, you need to create a separate database and user in MongoDB for Metal. For example, to create a user named metal and metalDB database, you can execute the following command in `mongosh`,

```shell
use metalDB
db.createUser({
  user: 'metal', 
  pwd: '123456', 
  roles: [{role: 'root', db: 'admin'}]
})
```

> The root user password of MongoDB in the quick-start environment is 123456, the address is 192.168.42.50, and the port is 27017.

Next, you need to import the db script in the build directory to the database. Just execute the following command,

```shell
ls ../../build/sbin/db | awk -F '.' '{print $1}' | xargs -I {} mongoimport -c {} --type json --file ../../build/sbin/db/{}.json mongodb://<credentials>@<host>:<port>/metalDB
```

### Configure

After the project is compiled and packaged, the relevant configuration files will be copied to the `$METAL/build/conf` directory. If you use custom MongoDB and Zookeeper, you need to modify the relevant configuration files, otherwise skip this part.
- MongoDB: The following items in `$METAL/build/conf/conf/metal-server.json` need to be modified to the service configuration you use.

```json
{
  ...
  "mongoConf": {
    "connection_string": "mongodb://<credentials>@<host>:<port>/<CUSTOM_DB>"
  },
  ...
}
```

- Zookeeper: The following items in `$METAL/build/conf/zookeeper.json` need to be modified to the service configuration you use.

  ```json
  {
    ...
    "zookeeperHosts": "<ZOOKEEPER_HOST>",
    ...
  }
  ```

## Start Metal Server

In the root directory of the project, execute the following command to complete the startup.

```shell
java -cp ./build/libs/metal-server-{VERSION}.jar:./build/ui:./build/conf org.metal.server.GatewayLauncher
```

Finally, open the browser and access the link `http://localhost:19000`.

> The test username is jack, and the password is 123456.

