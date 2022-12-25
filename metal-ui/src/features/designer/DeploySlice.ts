import { StoreApi } from "zustand";
import { BackendStatus } from "../../model/Project";

export interface DeploySlice {
    deployId?: string,
    epoch?: number,
    backendStatus?: BackendStatus,
    bindDeployId: (id: string) => void;
    bindEpoch: (epoch: number) => void;
    bindBackendStatus: (status: BackendStatus) => void;
    bindDeploy: (id?: string, epoch?: number) => void;
}

export const createDeploySlice = (
    set: StoreApi<DeploySlice>['setState'],
    get: StoreApi<DeploySlice>['getState'],
): DeploySlice => ({
    bindDeployId: (id: string) => {
        set((prev) => ({
            deployId: id
        }));
    },
    bindEpoch: (epoch: number) => {
        set((prev) => ({
            epoch: epoch
        }));
    },
    bindBackendStatus: (status: BackendStatus) => {
        set((prev) => ({
            backendStatus: status
        }));
    },
    bindDeploy: (id?: string, epoch?: number) => {
        set((prev) => ({
            deployId: id,
            epoch: epoch
        }));
    }
})