# Metal

<img src="/metal-ui/public/images/metal_brand.svg" alt="metal logo" height="200px" align="center" />

---

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md) [![CN doc](https://img.shields.io/badge/document-Chinese-yellow.svg)](README_cn.md)
[![Check Format](https://github.com/CheneyYin/metal/actions/workflows/check_format.yml/badge.svg)](https://github.com/CheneyYin/metal/actions/workflows/check_format.yml)
[![Build](https://github.com/CheneyYin/metal/actions/workflows/maven_build.yml/badge.svg)](https://github.com/CheneyYin/metal/actions/workflows/maven_build.yml)

---
## What is Metal?

Metal是一款数据流建模软件，通过Metal可以管理数据流处理算子、可视化建模、批处理任务执行。


## What Metal can do for you?

如果你经常使用Spark SQL开发ETL Pipeline，积累了大量的DTD(Dataframe To Dataframe)的算子/操作，你可以按照Metal的插件规范改造你的算子/操作，使用Metal来管理这些插件。

如果你使用Metal，你可以很方便地复用这些插件。Metal提供了两种方式来构建数据流，数据流是由插件组合而成的。

- 第一种构建方式是Cli风格，你需要编写spec文件来配置数据流的结构以及数据处理算子的参数。
- 第二方式是可视化风格，Metal提供了用于数据流设计的Web UI即metal-ui。metal-ui是一个简易的数据流集成开发环境，相比于Cli风格，metal-ui降低了配置数据流的难度。metal-ui以Project的概念管理每个数据流。你可以在metal-ui中，创建Project、配置Project、绘制数据流、跟踪数据处理任务、管理算子插件等。

## Features
- 支持Spark SQL批处理引擎
- 处理算子复用和管理
- 支持`spark-submit`命令行提交
- 提供了REST-API服务
- 支持可视化构建数据流
- 支持算子扩展
- 提供了打包工具
- 提供了Web-UI
- 支持用户级、Project级资源隔离

## Quick Start
[Quick Start](/doc/Quick_Start_cn.md)

## Architecture
[Architecture](/doc/Architecture_cn.md)

## How to Contribute

感谢您对本项目贡献的兴趣! 了解如何对项目做贡献的最简单的方式是提交一次PR。在正式发起PR前，你需要了解如何构建源码以及提交前的主要事项。

### 从源码构建

构建Metal，需要使用JDK 11及以上版本。从仓库拉取最新代码，然后使用`maven`执行构建：

```shell
git pull origin master
mvn clean package -pl metal-dist -am -Dmaven.test.skip=true 
```

### 提交PR前的注意项

通过如下命令检查代码格式:

```shell
mvn spotless:check
```

您需要保证在发起PR前，已经修复了全部`spotless`错误。

更多细节见[Contributing.md](CONTRIBUTING.md).

## Code of Conduct

[CODE_OF_CONDUCT](CODE_OF_CONDUCT.md)

## Security

[Security.md](SECURITY.md)
## Sponsor

<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains Logo (Main) logo.">
<strong>Thanks to JetBrains for the <a target="_blank" href="https://jb.gg/OpenSourceSupport">free license</a>.</strong><br>

## License

[Apache 2.0 License.](LICENSE)
