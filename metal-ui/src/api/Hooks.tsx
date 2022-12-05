import { useCallback, useState } from "react";
import { State } from "./State";

interface AsyncState<R> {
    status: State;
    result: R | null;
    error: any | null;
}

export function useAsync<R>() {
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
            }, 1000);
        } catch (reason) {
            setState({
                status: State.failure,
                result: null,
                error: reason,
            });
            console.log(reason);
        }
    }, []);

    return [run, state.status, state.result, state.error];
}
