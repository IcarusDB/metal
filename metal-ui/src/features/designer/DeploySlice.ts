import { StoreApi } from "zustand";
import { Exec } from "../../model/Exec";
import { BackendStatus } from "../../model/Project";

export interface DeploySlice {
    deployId?: string,
    epoch?: number,
    backendStatus?: BackendStatus,
    exec?: Exec,
    getDeployId: () => string | undefined;
    setDeployId: (id: string) => void;
    getEpoch: () => number | undefined;
    setEpoch: (epoch: number) => void;
    setBackendStatus: (status: BackendStatus) => void;
    getBackendStatus: () => BackendStatus | undefined;
    setDeploy: (id?: string, epoch?: number) => void;
    getDeploy: () => {id?: string, epoch?: number};
    setExec: (exec: Exec | undefined) => void;
    getExec: () => Exec | undefined;
}

export const createDeploySlice = (
    set: StoreApi<DeploySlice>['setState'],
    get: StoreApi<DeploySlice>['getState'],
): DeploySlice => ({
    getDeployId: () => (get().deployId),
    setDeployId: (id: string) => {
        set((prev) => ({
            ...prev,
            deployId: id
        }));
    },
    getEpoch: () => (get().epoch),
    setEpoch: (epoch: number) => {
        set((prev) => ({
            ...prev,
            epoch: epoch
        }));
    },
    setBackendStatus: (status: BackendStatus) => {
        set((prev) => ({
            ...prev,
            backendStatus: status
        }));
    },
    getBackendStatus: () => (get().backendStatus),
    setDeploy: (id?: string, epoch?: number) => {
        set((prev) => ({
            ...prev,
            deployId: id,
            epoch: epoch
        }));
    },
    getDeploy: () => ({deployId: get().deployId, epoch: get().epoch}),
    setExec: (exec: Exec | undefined) => {
        set((prev) => ({
            ...prev,
            exec
        }));
    },
    getExec: () => (get().exec),
})