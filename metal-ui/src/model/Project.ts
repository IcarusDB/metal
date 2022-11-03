import {Spec} from "./Spec";

export interface Deploy {
    id: string,
    epoch: number,
    pkgs: string[],
    platform: any,
    backend: {
        args: string[],
        status?:{
            current: string,
            upTime?: number,
            downTime?: number,
            failureTime?: number,
            failureMsg?: string,
            tracer: any
        }
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