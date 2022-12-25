import { StoreApi } from "zustand";

export interface DeploySlice {
    deployId?: string,
    epoch?: number,
    bindDeployId: (id: string) => void;
    bindEpoch: (epoch: number) => void;
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
    bindDeploy: (id?: string, epoch?: number) => {
        set((prev) => ({
            deployId: id,
            epoch: epoch
        }));
    }
})