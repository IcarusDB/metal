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