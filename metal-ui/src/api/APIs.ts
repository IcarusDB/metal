import { AxiosError } from "axios";

export const Gateway = {
    prefix: 'http',
    host: "localhost",
    port:19000,
    timeout: 20000
}

export function wrapUrl(url: string) {
    return Gateway.prefix + "://" + Gateway.host + ":" + Gateway.port + url;
}

export function baseUrl() {
    return Gateway.prefix + "://" + Gateway.host + ":" + Gateway.port
}

export function timeout() {
    return Gateway.timeout
}

export interface ApiResponseEntity {
    status: string,
    data?: any
    msg?: string
}

export enum ApiResponseStatus {
    success = 'OK',
    failure = 'FAIL'
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
                throw new AxiosError("Response is failure, and no msg found in response.");
            }
            throw new AxiosError(response.msg);
        }
        if (response.data === undefined) {
            throw new AxiosError("Response is successful, but no data found in response.");
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
};