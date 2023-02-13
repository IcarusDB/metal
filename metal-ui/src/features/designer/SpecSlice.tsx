import { Spec } from "../../model/Spec";
import { StoreApi } from "zustand";
import { SpecFlow } from "./SpecLoader";

export interface SpecSlice {
    spec?: Spec;
    flow?: SpecFlow;
    bindSpec: (spec: Spec) => void;
    bindFlow: (flow: SpecFlow) => void;
}
export const createSpecSlice = (
    set: StoreApi<SpecSlice>["setState"],
    get: StoreApi<SpecSlice>["getState"]
): SpecSlice => ({
    bindSpec: (spec: Spec) => {
        set((prev: SpecSlice) => ({ ...prev, spec: spec }));
    },
    bindFlow: (flow: SpecFlow) => {
        set((prev: SpecSlice) => ({...prev, flow: flow}));
    }
});
