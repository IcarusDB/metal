# What is Metal?

Metal是一款数据流建模软件，通过Metal可以管理数据流处理算子、可视化建模、批处理任务执行。


# What Metal can do for you?

如果你经常使用Spark SQL开发ETL Pipeline，积累了大量的DTD(Dataframe To Dataframe)的算子/操作，你可以按照Metal的插件规范改造你的算子/操作，使用Metal来管理这些插件。

如果你使用Metal，你可以很方便地复用这些插件。Metal提供了两种方式来构建数据流，数据流是由插件组合而成的。

- 第一种构建方式是Cli风格，你需要编写spec文件来配置数据流的结构以及数据处理算子的参数。
- 第二方式是可视化风格，Metal提供了用于数据流设计的Web UI即metal-ui。metal-ui是一个简易的数据流集成开发环境，相比于Cli风格，metal-ui降低了配置数据流的难度。metal-ui以Project的概念管理每个数据流。你可以在metal-ui中，创建Project、配置Project、绘制数据流、跟踪数据处理任务、管理算子插件等。

# Features
- 支持Spark SQL批处理引擎
- 处理算子复用和管理
- 支持`spark-submit`命令行提交
- 提供了REST-API服务
- 支持可视化构建数据流
- 支持算子扩展
- 提供了打包工具
- 提供了Web-UI
- 支持用户级、Project级资源隔离

# Quick Start
[Quick Start](/doc/Quick_Start.md)

# Architecture
[Architecture](/doc/Architecture.md)