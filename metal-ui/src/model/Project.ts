import {Spec} from "./Spec";

export enum BackendState {
    UN_DEPLOY= 'UN_DEPLOY',
    UP = 'UP',
    DOWN = 'DOWN',
    FAILURE = 'FAILURE'
}

export interface BackendStatus {
    current: BackendState,
    upTime?: number,
    downTime?: number,
    failureTime?: number,
    failureMsg?: string,
    tracer: any
}

export enum PlatformType {
    SPARK_STANDALONE = "spark.standalone"
}

export function platformType(type: string) {
    switch (type) {
        case PlatformType.SPARK_STANDALONE: {
            return PlatformType.SPARK_STANDALONE;
        }    
        default:{
            return PlatformType.SPARK_STANDALONE
        }    }
}

export function platformSchema(type: PlatformType) {
    switch (type) {
        case PlatformType.SPARK_STANDALONE: {
            return {
                type: "object",
                properties: {
                    "rest.api": {
                        type: "object",
                        properties: {
                            "host": {
                                type: "string"
                            },
                            "port": {
                                type: "string"
                            },
                            "requestURI": {
                                type: "object",
                                "create": {
                                    type: "string",
                                    default: "/v1/submissions/create"
                                },
                                "status": {
                                    type: "string",
                                    default: "/v1/submissions/status/{driverId}"
                                },
                                "kill": {
                                    type: "string",
                                    default: "/v1/submissions/kill/{driverId}"
                                }
                              }
                        }
                    },
                    "conf": {
                        type: "object",
                    }
                }
            }
        }
    }
}

export interface Deploy {
    id: string,
    epoch: number,
    pkgs: string[],
    platform: any,
    backend: {
        args: string[],
        status?: BackendStatus
    }
}

export interface Project {
    id: string,
    name: string,
    user: {
        id: string,
        username: string
    },
    deploy: Deploy,
    spec: Spec
}