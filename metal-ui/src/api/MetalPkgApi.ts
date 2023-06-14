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
import { ApiResponse, ApiResponseEntity, timeout } from "./APIs";
import { MetalManifest, MetalPkg, Scope } from "../model/MetalPkg";

const instance = axios.create({
  headers: {
    "Content-Type": "application/json",
  },
  timeout: timeout(),
});

export async function getAllMetalPkgsOfUserAccess(
  token: string
): Promise<MetalPkg[]> {
  const url = "/api/v1/metalRepo/all";
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

        const result: MetalPkg[] = resp.data.map((obj: any) => {
          obj = _.mapKeys(obj, (val, key) => {
            return key === "_id" ? "id" : key;
          });
          const pkg: MetalPkg = obj;
          return pkg;
        });
        return result;
      } catch (err) {
        return Promise.reject(err);
      }
    });
}

export async function getAllMetalPkgsOfClasses(
  token: string,
  classes: string[]
): Promise<MetalPkg[]> {
  const url = "/api/v1/metalRepo/classes";
  return instance
    .post(
      url,
      {
        classes: classes,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);

        const result: MetalPkg[] = resp.data.map((obj: any) => {
          obj = _.mapKeys(obj, (val, key) => {
            return key === "_id" ? "id" : key;
          });
          const pkg: MetalPkg = obj;
          return pkg;
        });
        return result;
      } catch (err) {
        return Promise.reject(err);
      }
    });
}

export async function addMetalPkgsFromManifest(
  token: string,
  scope: Scope,
  manifest: MetalManifest
): Promise<void> {
  const url = `api/v1/metalRepo/scope/${scope}`;
  return instance
    .post(
      url,
      {
        manifest: manifest,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
    .then((response) => {
      try {
        const resp: ApiResponseEntity = response.data;
        ApiResponse.mayBeFailure(resp);
      } catch (err) {
        return Promise.reject(err);
      }
    });
}
