import shallow from 'zustand/shallow';
import { ApiResponse } from "../../api/APIs";
import { IAsyncCallback, useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { Logger, loggerSelector, useNotice } from "../notice/Notice";

export function useUIAsync<R>(callback?: IAsyncCallback<R>): [(promise: Promise<R>) => Promise<void>,State, R | null, any] {
    const {warning, error} = useNotice<Logger>(loggerSelector, shallow);
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
        }
    });
}