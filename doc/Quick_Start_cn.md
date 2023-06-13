# Quick Start

<img src="/metal-ui/public/images/metal_logo.svg" alt="metal logo" height="200px" align="right" />

---

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](/doc/Quick_Start.md) [![CN doc](https://img.shields.io/badge/document-Chinese-yellow.svg)](/doc/Quick_Start_cn.md)

---

为了快速体验Metal，你可通过以下安装方式建立一个测试体验环境。

## 预先准备

- OpenJDK 11及以上版本。
- npm 9.5.0及以上版本。
- Docker
- docker-compose
- maven
- mongodb tools

## 编译打包

在Metal项目根目录下，执行如下命令完成编译打包。

```shell
mvn clean package -pl metal-dist -am -Dmaven.test.skip=true 
```
如果你在执行后遇到与`spotless`相关的错误信息，请执行`mvn spotless:apply`，然后再次重新执行打包命令。

打包出的内容保存在`/metal-dist/target/metal-dist-${version}`目录下。

```shell
./metal-dist/target/metal-dist-1.0.0-SNAPSHOT-bin
├── conf
│   ├── backend
│   │   ├── log4j.properties
│   │   ├── META-INF
│   │   └── zookeeper.json
│   ├── conf
│   │   └── metal-server.json
│   ├── log4j.properties
│   ├── metal-server.openapi.json
│   └── zookeeper.json
├── doc
│   ├── Architecture_cn.md
│   ├── Architecture.md
│   ├── img
│   │   └── resources
│   ├── Quick_Start_cn.md
│   └── Quick_Start.md
├── libs
...
│   ├── metal-backend-1.0.0-SNAPSHOT.jar
│   ├── metal-backend-api-1.0.0-SNAPSHOT.jar
│   ├── metal-core-1.0.0-SNAPSHOT.jar
│   ├── metal-on-spark-1.0.0-SNAPSHOT.jar
│   ├── metal-on-spark-extensions-1.0.0-SNAPSHOT.jar
│   ├── metal-server-1.0.0-SNAPSHOT.jar
...

├── LICENSE
├── README_cn.md
├── README.md
├── sbin
│   └── db
│       ├── execs.json
│       ├── metals.json
│       ├── project.json
│       └── user.json
├── tools
│   └── metal-maven-plugin-1.0.0-SNAPSHOT.jar
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

## 准备运行依赖环境

### 启动服务

进入项目根目录下的`./metal-test/docker`目录，通过如下命令启动相关依赖服务（包括MongoDB、MongoDB Express、Zookeeper、Spark Standalone集群和HDFS）

```shell
docker-compose up -d
```

当然如果你不打算执行任何任务，只是想体验一下Metal的效果，你可以只启动MongoDB和Zookeeper。执行如下命令即可。

```shell
docker-compose up -d mongo zoo0
```

### 初始化MongoDB

首先，你需要在MongoDB中为Metal创建一个单独的数据库和用户。比如创建用户名为metal的用户和metalDB数据库，你可以在`mongosh`中执行如下命令，

```shell
use metalDB
db.createUser({
  user: 'metal', 
  pwd: '123456', 
  roles: [{role: 'root', db: 'admin'}]
})
```

> 体验环境中的MongoDB的root用户密码为123456，地址为192.168.42.50，端口为27017。

接下来，你需要将构建目录下的db脚本导入到数据库。执行如下命令即可，

```shell
ls ../../metal-dist/target/metal-dist-${version}/sbin/db | awk -F '.' '{print $1}' | xargs -I {} mongoimport -c {} --type json --file ../../metal-dist/target/metal-dist-${version}/sbin/db/{}.json mongodb://<credentials>@<host>:<port>/metalDB
```

### 配置

项目在编译打包后，会将相关配置文件复制到`$METAL/metal-dist/target/metal-dist-${version}/conf`目录下。如果你使用了自定义的MongoDB和Zookeeper，你需要修改相关配置文件，否则跳过该部分。
- MongoDB：`$METAL/metal-dist/target/metal-dist-${version}/conf/conf/metal-server.json` 的如下几项需要修改为你提供的服务配置。

```json
{
  ...
  "mongoConf": {
    "connection_string": "mongodb://<credentials>@<host>:<port>/<CUSTOM_DB>"
  },
  ...
}
```

- Zookeeper：`$METAL/metal-dist/target/metal-dist-${version}/conf/zookeeper.json` 的如下几项需要修改为你提供的服务配置。

  ```json
  {
    ...
    "zookeeperHosts": "<ZOOKEEPER_HOST>",
    ...
  }
  ```

## 启动Metal

在项目根目录下，执行如下命令完成启动。

```shell
java -cp ./metal-dist/target/metal-dist-${version}/libs/metal-server-{VERSION}.jar:./metal-dist/target/metal-dist-${version}/ui:./metal-dist/target/metal-dist-${version}/conf org.metal.server.GatewayLauncher
```

最后打开浏览器，链接`http://localhost:19000`即可。

> 测试用户名jack，密码123456。

