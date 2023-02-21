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
