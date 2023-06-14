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


import { AxiosError } from "axios";

export const Gateway = {
  prefix: "http",
  host: "localhost",
  port: 19000,
  timeout: 300000,
};

export function wrapUrl(url: string) {
  return Gateway.prefix + "://" + Gateway.host + ":" + Gateway.port + url;
}

export function baseUrl() {
  return Gateway.prefix + "://" + Gateway.host + ":" + Gateway.port;
}

export function timeout() {
  return Gateway.timeout;
}

export interface ApiResponseEntity {
  status: string;
  data?: any;
  msg?: string;
}

export enum ApiResponseStatus {
  success = "OK",
  failure = "FAIL",
}

export const ApiResponse = {
  isSuccess: (response: ApiResponseEntity) => {
    try {
      return response.status === ApiResponseStatus.success;
    } catch (err) {
      return false;
    }
  },
  isFailure: (response: ApiResponseEntity) => {
    try {
      return response.status === ApiResponseStatus.failure;
    } catch (err) {
      return false;
    }
  },
  mayBeFailure: (response: ApiResponseEntity) => {
    if (!ApiResponse.isSuccess(response)) {
      if (response.msg === undefined) {
        throw new AxiosError(
          "Response is failure, and no msg found in response."
        );
      }
      throw new AxiosError(response.msg);
    }
    if (response.data === undefined) {
      throw new AxiosError(
        "Response is successful, but no data found in response."
      );
    }
  },
  extractErrorBreif: (error: AxiosError) => {
    if (error.isAxiosError) {
      return error.message;
    }
  },
  extractErrorMessage: (error: AxiosError<ApiResponseEntity>) => {
    if (error.isAxiosError) {
      const errorResp: ApiResponseEntity | undefined = error.response?.data;
      if (errorResp !== undefined) {
        return errorResp.msg;
      }
    }
  },
  extractMetalIds: (errorMsg: string) => {
    const matchResult = errorMsg.match(
      /^[a-zA-Z0-9\\.]+: Metal\[([a-zA-Z0-9_\-{},]+)\]/
    );
    if (matchResult !== null && matchResult?.length >= 1) {
      const metals = matchResult[1].match(/^{[a-zA-Z0-9_\-,]+}$/);
      const metalIds =
        metals === null
          ? [matchResult[1]]
          : matchResult[1].replace("{", "").replace("}", "").split(",");
      return metalIds;
    }
    return undefined;
  },
};
