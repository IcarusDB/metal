import { ApiResponse } from "../../api/APIs";
import { IAsyncCallback, useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { useMessagsLogger } from "./DesignerProvider";

export function useDesignerAsync<R>(callback?: IAsyncCallback<R>): [
    (promise: Promise<R>) => Promise<void>,
    State, 
    R | null, 
    any
] {
    const {warning, error} = useMessagsLogger();
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