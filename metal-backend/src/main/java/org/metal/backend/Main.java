package org.metal.backend;

public class Main {

  public static void main(String[] args) {
      String metalJars = "/home/spark/metal/commons-collections4-4.2.jar,/home/spark/metal/bson-4.1.2.jar,/home/spark/metal/jackson-module-jsonSchema-2.13.3.jar,/home/spark/metal/vertx-bridge-common-4.3.3.jar,/home/spark/metal/vertx-config-4.3.3.jar,/home/spark/metal/jsr305-3.0.0.jar,/home/spark/metal/arrow-format-7.0.0.jar,/home/spark/metal/vertx-auth-jwt-4.3.3.jar,/home/spark/metal/commons-codec-1.15.jar,/home/spark/metal/netty-codec-http2-4.1.78.Final.jar,/home/spark/metal/netty-handler-proxy-4.1.78.Final.jar,/home/spark/metal/metal-core-1.0.0-SNAPSHOT.jar,/home/spark/metal/vertx-zookeeper-4.3.3.jar,/home/spark/metal/j2objc-annotations-1.3.jar,/home/spark/metal/flatbuffers-java-1.12.0.jar,/home/spark/metal/validation-api-1.1.0.Final.jar,/home/spark/metal/vertx-service-factory-4.3.3.jar,/home/spark/metal/zookeeper-jute-3.5.9.jar,/home/spark/metal/slf4j-log4j12-1.7.25.jar,/home/spark/metal/error_prone_annotations-2.5.1.jar,/home/spark/metal/jackson-core-2.12.5.jar,/home/spark/metal/jackson-core-2.13.2.jar,/home/spark/metal/netty-codec-http-4.1.78.Final.jar,/home/spark/metal/netty-common-4.1.74.Final.jar,/home/spark/metal/netty-codec-socks-4.1.78.Final.jar,/home/spark/metal/slf4j-api-1.7.32.jar,/home/spark/metal/metal-on-spark-1.0.0-SNAPSHOT.jar,/home/spark/metal/hamcrest-core-1.3.jar,/home/spark/metal/netty-transport-native-epoll-4.1.50.Final.jar,/home/spark/metal/commons-cli-1.5.0.jar,/home/spark/metal/curator-framework-4.3.0.jar,/home/spark/metal/vertx-core-4.3.3.jar,/home/spark/metal/netty-transport-4.1.78.Final.jar,/home/spark/metal/slf4j-api-1.7.25.jar,/home/spark/metal/vertx-codegen-4.3.3.jar,/home/spark/metal/vertx-web-4.3.3.jar,/home/spark/metal/netty-transport-native-unix-common-4.1.78.Final.jar,/home/spark/metal/guava-30.1.1-jre.jar,/home/spark/metal/error_prone_annotations-2.11.0.jar,/home/spark/metal/netty-resolver-4.1.78.Final.jar,/home/spark/metal/mongodb-driver-reactivestreams-4.1.2.jar,/home/spark/metal/arrow-vector-7.0.0.jar,/home/spark/metal/zookeeper-3.5.9.jar,/home/spark/metal/audience-annotations-0.5.0.jar,/home/spark/metal/jackson-annotations-2.13.3.jar,/home/spark/metal/vertx-mongo-client-4.3.3.jar,/home/spark/metal/vertx-auth-mongo-4.3.3.jar,/home/spark/metal/commons-codec-1.10.jar,/home/spark/metal/vertx-http-proxy-4.3.3.jar,/home/spark/metal/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar,/home/spark/metal/checker-qual-3.12.0.jar,/home/spark/metal/vertx-web-common-4.3.3.jar,/home/spark/metal/netty-resolver-dns-4.1.78.Final.jar,/home/spark/metal/checker-qual-3.8.0.jar,/home/spark/metal/netty-common-4.1.78.Final.jar,/home/spark/metal/netty-codec-4.1.78.Final.jar,/home/spark/metal/vertx-service-proxy-4.3.3.jar,/home/spark/metal/jsr305-3.0.2.jar,/home/spark/metal/netty-buffer-4.1.78.Final.jar,/home/spark/metal/arrow-memory-core-7.0.0.jar,/home/spark/metal/vertx-web-proxy-4.3.3.jar,/home/spark/metal/curator-recipes-4.3.0.jar,/home/spark/metal/curator-client-4.3.0.jar,/home/spark/metal/log4j-1.2.17.jar,/home/spark/metal/netty-common-4.1.68.Final.jar,/home/spark/metal/slf4j-log4j12-1.7.32.jar,/home/spark/metal/netty-handler-4.1.78.Final.jar,/home/spark/metal/metal-on-spark-extensions-1.0.0-SNAPSHOT.jar,/home/spark/metal/metal-backend-api-1.0.0-SNAPSHOT.jar,/home/spark/metal/jackson-core-2.13.3.jar,/home/spark/metal/commons-lang3-3.12.0.jar,/home/spark/metal/guava-31.1-jre.jar,/home/spark/metal/vertx-auth-common-4.3.3.jar,/home/spark/metal/mongodb-driver-core-4.1.2.jar,/home/spark/metal/netty-codec-dns-4.1.78.Final.jar,/home/spark/metal/reactive-streams-1.0.3.jar,/home/spark/metal/metal-backend-1.0.0-SNAPSHOT.jar,/home/spark/metal/junit-4.13.2.jar,/home/spark/metal/failureaccess-1.0.1.jar,/home/spark/metal/jackson-databind-2.13.3.jar";
      String[] argv = {
          "--class", "org.metal.backend.BackendLauncher",
          "--master", "spark://192.168.41.70:7077",
          "--deploy-mode", "cluster",
          "--jars", metalJars,
          "--conf", "spark.executor.userClassPathFirst=true",
          "--conf", "spark.driver.userClassPathFirst=true",
          "/home/spark/metal/metal-backend-1.0.0-SNAPSHOT.jar",
          "--conf", "appName=Test",
          "--cmd-mode",
          "--spec-file",
          "/home/spark/metal/spec.json"
      };

      BackendDeployManager.getBackendDeploy()
          .get().deploy(argv);
  }
}
