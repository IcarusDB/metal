import {useCallback, useState } from "react";
import { State } from "./State";

export function useAsync<R>() {
    const [status, setStatus] = useState<State>(State.idle);
    const [result, setResult] = useState<R | null>(null);
    const [error, setError] = useState<any>(null);



    const run = useCallback(async (promise: Promise<R>) => {
        if (!promise || !promise.then) {
            throw new Error("parameter promise is null or promise.then is null.")
        }
        setStatus(State.pending);
        try {
            const res = await promise;
            setResult(res);
            setStatus(State.success);
            setError(null);
        } catch (reason) {
            setResult(null);
            setStatus(State.failure);
            setError(reason);
        }
    }, [])

    return [run, status, result, error]
}
