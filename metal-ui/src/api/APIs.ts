export const Gateway = {
    prefix: 'http',
    host: "localhost",
    port:19000,
    timeout: 2000
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
            return response.status == ApiResponseStatus.success
        } catch (err) {
            return false
        }
    },
    isFailure: (response: ApiResponseEntity) => {
        try {
            return response.status == ApiResponseStatus.failure
        } catch (err) {
            return false
        }
    }
}