import { useCallback, useState } from "react";
import { useNotice } from "../features/notice/Notice";
import { State } from "./State";

interface AsyncState<R> {
    status: State;
    result: R | null;
    error: any | null;
}

export function useAsync<R>(): [(promise: Promise<R>) => Promise<void>,State, R | null, any] {
    const notice = useNotice((state) => (state.error));
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
        try {
            const res = await promise;
            setTimeout(() => {
                setState({
                    status: State.success,
                    result: res,
                    error: null,
                });
            }, 2000);
        } catch (reason) {
            setState({
                status: State.failure,
                result: null,
                error: reason,
            });
            console.log(reason);
            notice(JSON.stringify(reason));
        }
    }, [notice]);

    return [run, state.status, state.result, state.error];
}
