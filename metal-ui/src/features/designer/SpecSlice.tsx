import { Spec } from "../../model/Spec";
import { StoreApi } from "zustand";
import { SpecFlow } from "./SpecLoader";

export interface SpecSlice {
    spec?: Spec;
    flow?: SpecFlow;
    getSpec: () => Spec | undefined;
    setSpec: (spec: Spec) => void;
    getFlow: () => SpecFlow | undefined;
    setFlow: (flow: SpecFlow) => void;
}
export const createSpecSlice = (
    set: StoreApi<SpecSlice>["setState"],
    get: StoreApi<SpecSlice>["getState"]
): SpecSlice => ({
    getSpec: () => (get().spec),
    setSpec: (spec: Spec) => {
        set((prev: SpecSlice) => ({ ...prev, spec: spec }));
    },
    getFlow: () => (get().flow),
    setFlow: (flow: SpecFlow) => {
        set((prev: SpecSlice) => ({...prev, flow: flow}));
    }
});
