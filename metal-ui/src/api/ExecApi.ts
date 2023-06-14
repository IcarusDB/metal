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


import axios from "axios";
import _ from "lodash";
import { Exec } from "../model/Exec";
import { ApiResponse, ApiResponseEntity, timeout } from "./APIs";
import { idMap } from "./IdMap";

const instance = axios.create({
  headers: {
    "Content-Type": "application/json",
  },
  timeout: timeout(),
});

export async function getAllExecsOfUser(token: string): Promise<Exec[]> {
  const url = `/api/v1/execs/detail`;
  return instance
    .get(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
        const mFunc: (obj: any) => Exec = idMap;
        const execs: Exec[] = resp.data.map(mFunc);
        return execs;
      } catch (err) {
        return Promise.reject(err);
      }
    });
}

export async function getExecOfId(token: string, id: string): Promise<Exec> {
  const url = `/api/v1/execs/id/${id}/detail`;
  return instance
    .get(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
        const exec: Exec = idMap<Exec>(resp.data);
        return exec;
      } catch (err) {
        return Promise.reject(err);
      }
    });
}

export async function getRecentExecOfProject(
  token: string,
  id: string,
  deployId?: string,
  epoch?: number
): Promise<Exec | undefined> {
  const url = `/api/v1/execs/project/${id}/detail`;
  return instance
    .get(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
        const mFunc: (obj: any) => Exec = idMap;
        const execs: Exec[] = resp.data.map(mFunc);
        if (deployId === undefined || epoch === undefined) {
          return _.maxBy(execs, (exec) => exec.createTime);
        }
        const exec = _.maxBy(
          execs.filter(
            (exec) => exec.deploy.id === deployId && exec.deploy.epoch === epoch
          ),
          (exec) => exec.createTime
        );
        return exec;
      } catch (err) {
        return Promise.reject(err);
      }
    });
}

export interface RecoverRequest {
  execId: string;
}

export interface RecoverResponse {
  projectId: string;
}

export async function recoverProjectFromExec(token: string, id: string) {
  const url = `/api/v1/projects/recover/exec`;
  const request: RecoverRequest = {
    execId: id,
  };

  return instance
    .post(url, request, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
        const rt: RecoverResponse = resp.data;
        return rt;
      } catch (error) {
        return Promise.reject(error);
      }
    });
}

export interface RemoveExecResponse {}

export async function removeExec(token: string, id: string) {
  const url = `/api/v1/execs/id/${id}`;
  return instance
    .delete(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
        const rt: RemoveExecResponse = resp.data;
        return rt;
      } catch (error) {
        return Promise.reject(error);
      }
    });
}
