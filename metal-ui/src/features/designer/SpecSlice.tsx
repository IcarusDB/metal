import { Spec } from "../../model/Spec";
import { StoreApi } from "zustand";

export interface SpecSlice {
    spec?: Spec;
    bindSpec: (spec: Spec) => void;
}
export const createSpecSlice = (
    set: StoreApi<SpecSlice>["setState"],
    get: StoreApi<SpecSlice>["getState"]
): SpecSlice => ({
    bindSpec: (spec: Spec) => {
        set((prev: SpecSlice) => ({ ...prev, spec: spec }));
    },
});
