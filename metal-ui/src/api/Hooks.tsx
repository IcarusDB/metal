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

import { useCallback, useState } from "react";
import { State } from "./State";

interface AsyncState<R> {
    status: State;
    result: R | null;
    error: any | null;
}

export interface IAsyncCallback<R> {
    onPending?: () => void;
    onSuccess?: (result: R) => void;
    onError?: (reason: any) => void;
}

export function useAsync<R>(callback?: IAsyncCallback<R>): [(promise: Promise<R>) => Promise<void>,State, R | null, any] {
    const [state, setState] = useState<AsyncState<R>>({
        status: State.idle,
        result: null,
        error: null,
    });

    const run = useCallback(async (promise: Promise<R>) => {
        if (!promise || !promise.then) {
            throw new Error("parameter promise is null or promise.then is null.");
        }
        setState((prevState) => ({
            ...prevState,
            status: State.pending,
        }));

        if (callback?.onPending) {
            callback.onPending();
        }

        try {
            const res = await promise;
            setState({
                status: State.success,
                result: res,
                error: null,
            });

            if (callback?.onSuccess) {
                callback.onSuccess(res);
            }
        } catch (reason) {
            setState({
                status: State.failure,
                result: null,
                error: reason,
            });
            if (callback?.onError) {
                callback.onError(reason);
            }
        }
    }, [callback]);

    return [run, state.status, state.result, state.error];
}
