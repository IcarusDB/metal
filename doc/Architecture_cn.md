# Architecture

<img src="/metal-ui/public/images/metal_brand.svg" alt="metal logo" height="200px" align="center" />

---

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](/doc/Architecture.md) [![CN doc](https://img.shields.io/badge/document-Chinese-yellow.svg)](/doc/Architecture_cn.md)

---

<img src="img/resources/metal-arch.svg" width = "75%" alt="Metal Architecture" align="center" />

Metal项目目前包括如下几个组件，

```shell
.
├── metal-backend
├── metal-backend-api
├── metal-core
├── metal-maven-plugin
├── metal-on-spark
├── metal-on-spark-extensions
├── metal-parent
├── metal-server
├── metal-test
└── metal-ui
```

各个组件概况如下：

## metal-core

`metal-core`是Metal的核心，它定义了Metal数据流处理算子类型、数据流分析检查以及执行。

### 数据流处理算子

#### 类型

Metal数据流处理算子包括四种，分别为Source、Mapper、Fusion和Sink。

<img src="img/resources/metal-plugin-class.svg" width = "75%" alt="Data Flow Operator Types" align="center" />


从上图可以看出，所有算子/操作的最终父类为`Metal`，所有继承`Metal`的子类都有`id`、`name`以及配置属性`props`。其中，`props`用于为算子注入配置。`metal-core`下的Metal抽象算子是平台无关的。目前，Metal仅支持将Spark作为后端执行引擎。
上图中`MSource`、`MMapper`、`MFusion`和`MSink`定义了数据流的四种基本处理逻辑。

- `MSource`: 用于获取数据，没有数据输入，有输出。例如`JsonFileSource`是`MSource`在Spark上的一种实现。`JsonFileSource`用从文件中加载JSON格式的数据，返回一个带有模式的DataFrame。
- `MMapper`：用于处理数据，拥有一个输入，并且有输出。例如`SqlMMapper`是`MMapper`在Spark上的一种实现，能够让用户把输入的DataFrame作为一张表，编写SQL处理输入的DataFrame处理得到新的DataFrame。
- `MFusion`：和`MMapper`类似都是用于处理数据，但是`MFusion`必须有两个及以上的输入。例如`SqlMFusion`，它的功能和`SqlMMapper`类似，但是必须输入两张及以上的DataFrame。
- `MSink`：用于下沉数据（写入文件、消息队列等），拥有一个输入，没有输出。例如`ConsoleMSink`是`MSink`在Spark上一种实现，能够把输入的DataFrame输出在控制台上，常用于调试。

#### 扩展

Metal提供了算子扩展能力，你需要按照如下步骤实现新的`Metal`子类，然后打包注册即可。

- 首先，根据算子输入、输出的数量来选择算子类型。例如，你需要实现拥有一个输入、一个输出的处理算子，那么你应该选择`MMapper`作为算子的父类。
- 然后，实现一个`MMapper`的子类。如果你的算子是封装的Spark SQL处理逻辑，那么你可以直接实现`SparkMMapper`的子类。
- 最后，你需要在你的工程中配置`metal-maven-plugin`或者直接使用`metal-maven-plugin`命令行来生成注册清单`manifest.json`。向Metal提交注册清单即可完成注册。

### 数据流Spec

数据流处理结构和算子配置定义在数据流Spec。数据流Spec包括`version`、`metals`、`edges`、`waitFor`四个部分。

- `version`：Spec的版本号。
- `metals`：定义了一组Metal，每个Metal定义中包含了Metal的类型、id、名称和其它算子配置信息。
- `edges`：定义了一组数据流向。每个edge包含了起止Metal的id。在`eges`可以建立一个数据流的DAG。
- `waitFor`：定义了`MSink`执行优先级的DAG。

如下是一个简单的数据流Spec示例，

```json
{
  "version" : "1.0",
  "metals" : [ {
    "type" : "org.metal.backend.spark.extension.JsonFileMSource",
    "id" : "00-00",
    "name" : "source-00",
    "props" : {
      "schema" : "",
      "path" : "src/test/resources/test.json"
    }
  }, {
    "type" : "org.metal.backend.spark.extension.SqlMMapper",
    "id" : "01-00",
    "name" : "mapper-00",
    "props" : {
      "tableAlias" : "source",
      "sql" : "select * from source where id != \"0001\""
    }
  }, {
    "type" : "org.metal.backend.spark.extension.ConsoleMSink",
    "id" : "02-00",
    "name" : "sink-00",
    "props" : {
      "numRows" : 10
    }
  }, {
    "type" : "org.metal.backend.spark.extension.ConsoleMSink",
    "id" : "02-01",
    "name" : "sink-01",
    "props" : {
      "numRows" : 10
    }
  } ],
  "edges" : [ {
    "left" : "00-00",
    "right" : "01-00"
  }, {
    "left" : "01-00",
    "right" : "02-00"
  }, {
    "left" : "00-00",
    "right" : "02-01"
  } ],
  "waitFor" : [ {
    "left" : "02-00",
    "right" : "02-01"
  } ]
}
```

<img src="img/resources/metal-spec.svg" width = "75%" alt="Data flow Spec example" align="center" />

在示例中，source-00会从`src/test/resources/test.json`获取JSON数据，mapper-00将source-00作为输入并且筛选出`id != "0001"`的记录，sink-00将mapper-00过滤出的数据打印在控制台。sink-00会直接将source-00的数据打印在控制台。sink-00会先于sink-01被调度执行。

## metal-backend

### 接口设计

`metal-backend`定义了metal的几种操作接口（`analyse`、`schema`、`heart`、`status`、`exec`等），并且实现了命令行模式和交互模式。

在交互模式下，`metal-backend`进程会启动REST服务和Vert.x RPC与`metal-server`通信。关于这几个接口，要做一些说明，

- `analyse`： 接收数据流Spec，检查规范性。然后将Spec构建成Spark DataFrame组成的DAG。为了优化性能，Spec中没有发生变化的处理算子会直接复用。这些变化包括算子配置的变化、输入的变化（算子名称的变化不会影响复用）。
- `schema`：获取算子输出DataFrame的模式。Metal中统一用Arrow Schema表示。
- `exec`：执行基于Spec构建的DAG。
- `heart`：发送心跳。
- `status`：访问服务状态。

交互模型下，`metal-backend`要处理并发请求，上述接口操作大部分是可以并发访问的，一部分只能串行访问。heart、可以并发访问，analyse、schema、status和exec需要做并发控制。并发控制关系如下，

|                | analyse | schema | status | exec       |
| -------------- | ------- | ------ | ------ | ---------- |
| [prev] analyse | 不并发  | 不并发 | 不并发 | ==不并发== |
| [prev] schema  | 不并发  | 并发   | 并发   | 并发       |
| [prev] status  | 不并发  | 并发   | 并发   | 并发       |
| [prev] exec    | 不并发  | 并发   | 并发   | 不并发     |

第1行第4列<analyse, exec>为不并发，这表示当analyse执行时，exec请求会被阻塞等待执行或直接方法。`metal-backend`定了`IBackendAPI`实现了这种并发控制，具体的设计图如下。

<img src="img/resources/metal-IBackendAPI.svg" width = "75%" alt="Class diagram of IBackendAPI" align="center" />

`IBackendTryAPI`中定义的接口为非阻塞接口。

### 运行模式
#### 命令行模式
`metal-backend`实现了命令行模式，你可在`spark-submit`提交任务时配置数据流。例如，

```shell
$SPARK_HOME/bin/spark-submit \
	--class org.metal.backend.BackendLauncher \
	--master spark://STANDALONE_MASTER_HOST:PORT \
	--jars $METAL_JARS \
	--conf spark.executor.userClassPathFirst=true \
	--conf spark.driver.userClassPathFirst=true \
	$METAL/metal-backend-1.0-SNAPSHOT.jar \
	--conf appName=Test \
	--cmd-mode \
	--spec-file $METAL_SPEC/spec.json
```

`--cmd-mode`表示开启命令行模式。 `--spec-file`表示定义的数据流Spec的URI，该URI需要Spark driver能够访问到。另外，你也可以使用`--spec`，在命令行中直接配置嵌入Spec的内容，例如，

```shell
$SPARK_HOME/bin/spark-submit \
	...
	$METAL/metal-backend-1.0-SNAPSHOT.jar \
	...
	--spec "{'version':'1.0', 'metals':[...], 'edges':[...], 'waitFor':[...]}"
```
数据流Spec中使用的算子所属的Jar文件也需要指明，你可将Jar文件的路径配置在`--jars`。

*目前，`metal-backend`仅在Spark Standalone集群中测试过，Yarn和K8S还未进行过验证。*

#### 交互模式

`metal-backend`实现了交互模式，当开了交互模式，`metal-backend`会启动REST服务和Vert.x RPC服务。例如：

```shell
./spark-submit \
	--class org.metal.backend.BackendLauncher \
	--master spark://STANDALONE_MASTER_HOST:PORT \
	--jars $METAL_JAR  \
	--conf spark.executor.userClassPathFirst=true \
	--conf spark.driver.userClassPathFirst=true \
	$METAL/metal-backend-1.0-SNAPSHOT.jar \
	--conf appName=Test \
	--interactive-mode \
	--deploy-id 35c0dbd3-0700-493f-a071-343bc0300bc9 \
	--deploy-epoch 0 \
	--report-service-address report.metal.org \
	--rest-api-port 18000
```

- `--interactive-mode`表示开启交互模式。
- `--deploy-id`为部署id，`--deploy-epoch`为部署纪元。部署id用于和`metal-server`通信，它能够关联到`metal-server`的一个Project；部署纪元则是为了提供了高可用而设计的，在`metal-server`为Project部署Backend时，会下发类似于上述示例的命令，来启动一个开启交互模式的 `metal-backend`，为了防止网络异常或启动异常带来的影响，每次为同一个Project部署Backend时，部署纪元都会增加。
- `--report-service-address`是`metal-server`提供的追踪Backend状态的RPC服务地址。
- `--rest-api-port`是`metal-backend`启动的REST API端口。

除了开发调试时，几乎不会手动编写交互模式的命令，正常情况下是`metal-server`根据你的Project配置来自动下发部署Backend。

## metal-server和metal-ui
### metal-server
#### 模型

<img src="img/resources/metal-server-model.svg" width = "75%" alt="Domain Entity Relationship Diagram of metal-server" align="center" />

`metal-server`建立了Project的概念来管理数据流以及相关配置。你可以在Project中选择需要的Metal算子，并且定义数据流。`metal-server`会根据配置部署执行后端，下发执行数据流，跟踪后端状态和数据流执行状态。

#### 服务
`metal-server`实现了Vert.x RPC和REST-API服务。
- Report服务：用于接收`metal-backend`上报的后端状态和任务执行状态。
- Failure Detector服务：追踪后端故障的检测服务，会将超时离线、进程关闭、延时过高的执行后端做下线处理。
- REST-API服务：为`metal-ui`提供的服务，包括创建Project、配置Project、保存数据流Spec、部署后端、执行数据流以及追踪后端状态等。

### metal-ui
`metal-ui`实现了Metal的前端Web UI。你可以在`metal-ui`中便捷地创建Project，选择算子包，绘制数据流，检查数据流，提交执行数据流，发布新的处理算子包，管理算子包等。

<img src="img/resources/metal-ui-home.png" width = "75%" alt="Home Page" align="center" />


<img src="img/resources/metal-ui-designer.png" width = "75%" alt="Data flow designer" align="center" />


<img src="img/resources/metal-ui-designer-profiler.png" width = "75%" alt="Data flow profiler" align="center" />


<img src="img/resources/metal-ui-repo.png" width = "75%" alt="Metal repository" align="center" />

`metal-ui`是一个简易的数据流IDE，`metal-ui`会调用`metal-server`提供的REST-API完成操作