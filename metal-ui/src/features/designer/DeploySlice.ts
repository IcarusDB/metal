import { StoreApi } from "zustand";
import { Exec } from "../../model/Exec";
import { BackendStatus } from "../../model/Project";

export interface DeploySlice {
    deployId?: string,
    epoch?: number,
    backendStatus?: BackendStatus,
    exec?: Exec,
    bindDeployId: (id: string) => void;
    bindEpoch: (epoch: number) => void;
    bindBackendStatus: (status: BackendStatus) => void;
    bindDeploy: (id?: string, epoch?: number) => void;
    bindExec: (exec: Exec | undefined) => void;
}

export const createDeploySlice = (
    set: StoreApi<DeploySlice>['setState'],
    get: StoreApi<DeploySlice>['getState'],
): DeploySlice => ({
    bindDeployId: (id: string) => {
        set((prev) => ({
            ...prev,
            deployId: id
        }));
    },
    bindEpoch: (epoch: number) => {
        set((prev) => ({
            ...prev,
            epoch: epoch
        }));
    },
    bindBackendStatus: (status: BackendStatus) => {
        set((prev) => ({
            ...prev,
            backendStatus: status
        }));
    },
    bindDeploy: (id?: string, epoch?: number) => {
        set((prev) => ({
            ...prev,
            deployId: id,
            epoch: epoch
        }));
    },
    bindExec: (exec: Exec | undefined) => {
        set((prev) => ({
            ...prev,
            exec
        }));
    }
})