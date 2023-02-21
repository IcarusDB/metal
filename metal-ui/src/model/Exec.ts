import { Spec } from "./Spec";

export enum ExecState {
    CREATE = "CREATE", 
    SUBMIT = "SUBMIT", 
    RUNNING = "RUNNING", 
    FINISH = "FINISH", 
    FAILURE = "FAILURE",
}

export interface ExecDeploy {
    id: string;
    epoch: number;
    pkgs: string[];
    platform: any;
    backend: {
        args: string[]
    };
}

export interface Exec {
    id: string,
    userId: string,
    deploy: ExecDeploy,
    fromProject: string,
    fromProjectDetail?: {
        name: string
    },
    SPEC: Spec,
    status: ExecState,
    createTime: number,
    submitTime?: number,
    beatTime?: number,
    finishTime?: number,
    terminateTime?: number,
}