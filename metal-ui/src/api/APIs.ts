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