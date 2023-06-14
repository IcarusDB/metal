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


import _ from "lodash";
import { BackendState, Project } from "./Project";

let proj = {
  id: "63561855e2b7291d712bd0b3",
  name: "test-proj",
  deploy: {
    id: "b45e0eff-eab8-4a4a-bcf1-d7497f4971ef",
    epoch: 1,
    pkgs: [],
    platform: {
      "spark.standalone": {
        "rest.api": {
          host: "master-0.spark.metal.org",
          port: 6066,
          requestURI: {
            create: "/v1/submissions/create",
            status: "/v1/submissions/status/{driverId}",
            kill: "/v1/submissions/kill/{driverId}",
          },
        },
        conf: {
          appResource: "/home/spark/metal/metal-backend-1.0.0-SNAPSHOT.jar",
          sparkProperties: {
            "spark.executor.userClassPathFirst": "true",
            "spark.driver.userClassPathFirst": "true",
            "spark.master": "spark://master-0.spark.metal.org:7077",
            "spark.app.name": "Spark REST API - Metal-Backend",
            "spark.submit.deployMode": "cluster",
            "spark.eventLog.enabled": "true",
            "spark.eventLog.dir":
              "hdfs://namenode.hdfs.metal.org:9000/shared/spark-logs",
            "spark.jars": "/home/spark/metal/metal-backend-1.0.0-SNAPSHOT.jar",
          },
          clientSparkVersion: "3.3.0",
          mainClass: "org.metal.backend.BackendLauncher",
          environmentVariables: {
            SPARK_ENV_LOADED: "1",
          },
          action: "CreateSubmissionRequest",
          appArgs: [],
        },
      },
    },
    backend: {
      args: [],
      status: {
        current: "DOWN" as BackendState,
        downTime: 1666586709997,
      },
    },
  },
  spec: {
    version: "1.0",
    metals: [
      {
        type: "org.metal.backend.spark.extension.JsonFileMSource",
        id: "00-00",
        name: "source-00",
        props: {
          schema: "",
          path: "hdfs://namenode.hdfs.metal.org:9000/metal/test.json",
        },
      },
      {
        type: "org.metal.backend.spark.extension.SqlMMapper",
        id: "01-00",
        name: "mapper-00",
        props: {
          tableAlias: "source",
          sql: 'select * from source where id != "0001"',
        },
      },
      {
        type: "org.metal.backend.spark.extension.ConsoleMSink",
        id: "02-00",
        name: "sink-00",
        props: {
          numRows: 10,
        },
      },
      {
        type: "org.metal.backend.spark.extension.ConsoleMSink",
        id: "02-01",
        name: "sink-01",
        props: {
          numRows: 10,
        },
      },
    ],
    edges: [
      {
        left: "00-00",
        right: "01-00",
      },
      {
        left: "01-00",
        right: "02-00",
      },
      {
        left: "00-00",
        right: "02-01",
      },
    ],
    waitFor: [
      {
        left: "02-00",
        right: "02-01",
      },
    ],
  },
  user: {
    id: "63490d88c8c0b246291970aa",
    username: "jack",
  },
};
test("test-project", () => {
  const project: Project = proj;
  console.log(project);
  expect(project).toBeTruthy();
});
