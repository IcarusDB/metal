/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
  getSpec: () => get().spec,
  setSpec: (spec: Spec) => {
    set((prev: SpecSlice) => ({ ...prev, spec: spec }));
  },
  getFlow: () => get().flow,
  setFlow: (flow: SpecFlow) => {
    set((prev: SpecSlice) => ({ ...prev, flow: flow }));
  },
});
