/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.backend;

public class Main {

    public static void main(String[] args) {
        //      String metalJars =
        // "../libs/commons-collections4-4.2.jar,../libs/bson-4.1.2.jar,../libs/jackson-module-jsonSchema-2.13.3.jar,../libs/vertx-bridge-common-4.3.3.jar,../libs/vertx-config-4.3.3.jar,../libs/jsr305-3.0.0.jar,../libs/arrow-format-7.0.0.jar,../libs/vertx-auth-jwt-4.3.3.jar,../libs/commons-codec-1.15.jar,../libs/netty-codec-http2-4.1.78.Final.jar,../libs/netty-handler-proxy-4.1.78.Final.jar,../libs/metal-core-1.0.0-SNAPSHOT.jar,../libs/vertx-zookeeper-4.3.3.jar,../libs/j2objc-annotations-1.3.jar,../libs/flatbuffers-java-1.12.0.jar,../libs/validation-api-1.1.0.Final.jar,../libs/vertx-service-factory-4.3.3.jar,../libs/zookeeper-jute-3.5.9.jar,../libs/slf4j-log4j12-1.7.25.jar,../libs/error_prone_annotations-2.5.1.jar,../libs/jackson-core-2.12.5.jar,../libs/jackson-core-2.13.2.jar,../libs/netty-codec-http-4.1.78.Final.jar,../libs/netty-common-4.1.74.Final.jar,../libs/netty-codec-socks-4.1.78.Final.jar,../libs/slf4j-api-1.7.32.jar,../libs/metal-on-spark-1.0.0-SNAPSHOT.jar,../libs/hamcrest-core-1.3.jar,../libs/netty-transport-native-epoll-4.1.50.Final.jar,../libs/commons-cli-1.5.0.jar,../libs/curator-framework-4.3.0.jar,../libs/vertx-core-4.3.3.jar,../libs/netty-transport-4.1.78.Final.jar,../libs/slf4j-api-1.7.25.jar,../libs/vertx-codegen-4.3.3.jar,../libs/vertx-web-4.3.3.jar,../libs/netty-transport-native-unix-common-4.1.78.Final.jar,../libs/guava-30.1.1-jre.jar,../libs/error_prone_annotations-2.11.0.jar,../libs/netty-resolver-4.1.78.Final.jar,../libs/mongodb-driver-reactivestreams-4.1.2.jar,../libs/arrow-vector-7.0.0.jar,../libs/zookeeper-3.5.9.jar,../libs/audience-annotations-0.5.0.jar,../libs/jackson-annotations-2.13.3.jar,../libs/vertx-mongo-client-4.3.3.jar,../libs/vertx-auth-mongo-4.3.3.jar,../libs/commons-codec-1.10.jar,../libs/vertx-http-proxy-4.3.3.jar,../libs/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar,../libs/checker-qual-3.12.0.jar,../libs/vertx-web-common-4.3.3.jar,../libs/netty-resolver-dns-4.1.78.Final.jar,../libs/checker-qual-3.8.0.jar,../libs/netty-common-4.1.78.Final.jar,../libs/netty-codec-4.1.78.Final.jar,../libs/vertx-service-proxy-4.3.3.jar,../libs/jsr305-3.0.2.jar,../libs/netty-buffer-4.1.78.Final.jar,../libs/arrow-memory-core-7.0.0.jar,../libs/vertx-web-proxy-4.3.3.jar,../libs/curator-recipes-4.3.0.jar,../libs/curator-client-4.3.0.jar,../libs/log4j-1.2.17.jar,../libs/netty-common-4.1.68.Final.jar,../libs/slf4j-log4j12-1.7.32.jar,../libs/netty-handler-4.1.78.Final.jar,../libs/metal-on-spark-extensions-1.0.0-SNAPSHOT.jar,../libs/metal-backend-api-1.0.0-SNAPSHOT.jar,../libs/jackson-core-2.13.3.jar,../libs/commons-lang3-3.12.0.jar,../libs/guava-31.1-jre.jar,../libs/vertx-auth-common-4.3.3.jar,../libs/mongodb-driver-core-4.1.2.jar,../libs/netty-codec-dns-4.1.78.Final.jar,../libs/reactive-streams-1.0.3.jar,../libs/metal-backend-1.0.0-SNAPSHOT.jar,../libs/junit-4.13.2.jar,../libs/failureaccess-1.0.1.jar,../libs/jackson-databind-2.13.3.jar";
        //      String[] argv = {
        //          "--class", "org.metal.backend.BackendLauncher",
        //          "--master", "spark://192.168.41.70:7077",
        //          "--deploy-mode", "cluster",
        //          "--jars", metalJars,
        //          "--conf", "spark.executor.userClassPathFirst=true",
        //          "--conf", "spark.driver.userClassPathFirst=true",
        //          "../libs/metal-backend-1.0.0-SNAPSHOT.jar",
        //          "--conf", "appName=Test",
        //          "--cmd-mode",
        //          "--spec-file",
        //          "../libs/spec.json"
        //      };
        //    String metalJars =
        // "../lib/metal-backend-1.0.0-SNAPSHOT.jar,../lib/metal-backend-api-1.0.0-SNAPSHOT.jar,../lib/metal-core-1.0.0-SNAPSHOT.jar,../lib/metal-on-spark-1.0.0-SNAPSHOT.jar,../lib/metal-on-spark-extensions-1.0.0-SNAPSHOT.jar,../lib/metal-server-1.0.0-SNAPSHOT.jar";
        //    String[] argv = {
        //        "--class", "org.metal.backend.BackendLauncher",
        //        "--master", "local[*]",
        //        "--deploy-mode", "cluster",
        //        "--jars", metalJars,
        //        "--conf", "spark.executor.userClassPathFirst=true",
        //        "--conf", "spark.driver.userClassPathFirst=true",
        //        "../lib/metal-backend-1.0.0-SNAPSHOT.jar",
        //        "--conf", "appName=Test",
        //        "--interactive-mode",
        //        "--deploy-id", "9422e1a4-73e1-4e99-afdf-19b04f25e9d3",
        //        "--deploy-epoch", "0",
        //        "--report-service-address", "report.metal.org",
        //        "--rest-api-port", "18989"
        //    };

        BackendDeployManager.getBackendDeploy().get().deploy(args);
    }
}
