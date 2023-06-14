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


import shallow from "zustand/shallow";
import { ApiResponse } from "../../api/APIs";
import { IAsyncCallback, useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { Logger, loggerSelector, useNotice } from "../notice/Notice";

export function useUIAsync<R>(
  callback?: IAsyncCallback<R>
): [(promise: Promise<R>) => Promise<void>, State, R | null, any] {
  const { warning, error } = useNotice<Logger>(loggerSelector, shallow);
  return useAsync<R>({
    ...callback,
    onError: (reason) => {
      const errorMsg = ApiResponse.extractErrorMessage(reason);
      if (errorMsg) {
        error(errorMsg);
      } else {
        const errorBreif = ApiResponse.extractErrorBreif(reason);
        if (errorBreif) {
          warning(errorBreif);
        }
      }
      if (callback?.onError) {
        callback?.onError(reason);
      }
    },
  });
}
