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

export enum ExecState {
  CREATE = "CREATE",
  SUBMIT = "SUBMIT",
  RUNNING = "RUNNING",
  FINISH = "FINISH",
  FAILURE = "FAILURE",
}

export interface ExecDeploy {
  id: string;
  epoch: number;
  pkgs: string[];
  platform: any;
  backend: {
    args: string[];
  };
}

export interface Exec {
  id: string;
  userId: string;
  deploy: ExecDeploy;
  fromProject: string;
  fromProjectDetail?: {
    name: string;
  };
  SPEC: Spec;
  status: ExecState;
  createTime: number;
  submitTime?: number;
  beatTime?: number;
  finishTime?: number;
  terminateTime?: number;
}
