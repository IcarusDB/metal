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


import { Spec } from "./Spec";

export enum BackendState {
  CREATED = "CREATED",
  UP = "UP",
  DOWN = "DOWN",
  FAILURE = "FAILURE",
}

export interface BackendStatus {
  current: BackendState;
  createdTime?: number;
  upTime?: number;
  downTime?: number;
  failureTime?: number;
  failureMsg?: string;
  tracer?: any;
  deployId?: string;
  epoch?: number;
}

export enum PlatformType {
  SPARK_STANDALONE = "spark.standalone",
}

export function platformType(type: string) {
  switch (type) {
    case PlatformType.SPARK_STANDALONE: {
      return PlatformType.SPARK_STANDALONE;
    }
    default: {
      return PlatformType.SPARK_STANDALONE;
    }
  }
}

export function platformSchema(type: PlatformType) {
  switch (type) {
    case PlatformType.SPARK_STANDALONE: {
      return {
        type: "object",
        properties: {
          "rest.api": {
            type: "object",
            properties: {
              host: {
                type: "string",
              },
              port: {
                type: "string",
              },
              requestURI: {
                type: "object",
                create: {
                  type: "string",
                  default: "/v1/submissions/create",
                },
                status: {
                  type: "string",
                  default: "/v1/submissions/status/{driverId}",
                },
                kill: {
                  type: "string",
                  default: "/v1/submissions/kill/{driverId}",
                },
              },
            },
          },
          conf: {
            type: "object",
          },
        },
      };
    }
  }
}

export interface Deploy {
  id: string;
  epoch: number;
  pkgs: string[];
  platform: any;
  backend: {
    args: string[];
    status: BackendStatus;
  };
}

export interface Project {
  id: string;
  name: string;
  user: {
    id: string;
    username: string;
  };
  deploy: Deploy;
  spec: Spec;
}
