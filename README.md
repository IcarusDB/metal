# Metal

<img src="/metal-ui/public/images/metal_brand.svg" alt="metal logo" height="200px" align="center" />

---

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md) [![CN doc](https://img.shields.io/badge/document-Chinese-yellow.svg)](README_cn.md)
[![Check Format](https://github.com/IcarusDB/metal/actions/workflows/check_format.yml/badge.svg)](https://github.com/IcarusDB/metal/actions/workflows/check_format.yml)
[![Build](https://github.com/IcarusDB/metal/actions/workflows/maven_build.yml/badge.svg)](https://github.com/IcarusDB/metal/actions/workflows/maven_build.yml)

---
## What is Metal?

Metal is a data flow modeling software that can manage data flow processing operators, visual modeling, and batch task execution through Metal.


## What Metal can do for you?

If you often use Spark SQL to develop ETL Pipeline and accumulate a large number of DTD (Dataframe To Dataframe) operators/operations, you can modify your operators/operations according to the Metal plugin specification and use Metal to manage these plugins.

If you use Metal, you can easily reuse these plugins. Metal provides two ways to build data streams, and data streams are composed of plugins.

- The first construction method is the Cli style. You need to write a spec file to configure the structure of the data stream and the parameters of the data processing operator.
- The second way is the visual style. Metal provides a Web UI for data flow design, namely metal-ui. Metal-ui is a simple data flow integrated development environment. Compared with the Cli style, metal-ui reduces the difficulty of configuring data flow. metal-ui manages each data flow with the concept of Project. In metal-ui, you can create projects, configure projects, draw data flows, track data processing tasks, manage operator plug-ins, and more.

## Features

- Support Spark SQL batch processing engine
- Supports multiplexing and management of processing operators
- Support `spark-submit` command line submission
- Provides REST-API service
- Support visual construction of data flow
- Support operator extension
- Provides a packaging tool
- Provides Web-UI
- Support user-level and project-level resource isolation

## Quick Start
[Quick Start](/doc/Quick_Start.md)

## Architecture
[Architecture](/doc/Architecture.md)

## How to Contribute

Thanks for your interest in contributing! The easiest way is to just send a pull request(PR). Before send a PR, you need to understand how to build the source code and do somethings.

### Building From source

Building Metal requires at minimum JDK 11. Pull the latest source from the repository and use Maven install (or package) to build:

```shell
git pull origin master
mvn clean package -pl metal-dist -am -Dmaven.test.skip=true 
```

### Before Send a pull request

Please check code format and fix the `spotless` errors if any:

```shell
mvn spotless:check
```

More details in [Contributing.md](CONTRIBUTING.md).

## Code of Conduct

[CODE_OF_CONDUCT](CODE_OF_CONDUCT.md)

## Security

[Security.md](SECURITY.md)
## Sponsor

<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains Logo (Main) logo.">
<strong>Thanks to JetBrains for the <a target="_blank" href="https://jb.gg/OpenSourceSupport">free license</a>.</strong><br>

## License
[Apache 2.0 License.](LICENSE)


