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


import { StoreApi } from "zustand";
import { Exec } from "../../model/Exec";
import { BackendStatus } from "../../model/Project";
import { Message } from "../notice/Notice";

export interface BackendSlice {
  projectId?: string;
  deployId?: string;
  epoch?: number;
  backendStatus?: BackendStatus;
  exec?: Exec;
  messages: Message[];
  getProjectId: () => string | undefined;
  setProjectId: (id: string) => void;
  getDeployId: () => string | undefined;
  setDeployId: (id: string) => void;
  getEpoch: () => number | undefined;
  setEpoch: (epoch: number) => void;
  setBackendStatus: (status: BackendStatus) => void;
  getBackendStatus: () => BackendStatus | undefined;
  setDeploy: (id?: string, epoch?: number) => void;
  getDeploy: () => { id?: string; epoch?: number };
  setExec: (exec: Exec | undefined) => void;
  getExec: () => Exec | undefined;
  getMessages: () => Message[];
  setMessages: (messages: Message[]) => void;
}

export const createBackendSlice = (
  set: StoreApi<BackendSlice>["setState"],
  get: StoreApi<BackendSlice>["getState"]
): BackendSlice => ({
  getProjectId: () => get().projectId,
  setProjectId: (id: string) => {
    set((prev) => ({
      ...prev,
      projectId: id,
    }));
  },
  getDeployId: () => get().deployId,
  setDeployId: (id: string) => {
    set((prev) => ({
      ...prev,
      deployId: id,
    }));
  },
  getEpoch: () => get().epoch,
  setEpoch: (epoch: number) => {
    set((prev) => ({
      ...prev,
      epoch: epoch,
    }));
  },
  setBackendStatus: (status: BackendStatus) => {
    set((prev) => ({
      ...prev,
      backendStatus: status,
    }));
  },
  getBackendStatus: () => get().backendStatus,
  setDeploy: (id?: string, epoch?: number) => {
    set((prev) => ({
      ...prev,
      deployId: id,
      epoch: epoch,
    }));
  },
  getDeploy: () => ({ deployId: get().deployId, epoch: get().epoch }),
  setExec: (exec: Exec | undefined) => {
    set((prev) => ({
      ...prev,
      exec,
    }));
  },
  getExec: () => get().exec,
  messages: [],
  getMessages: () => get().messages,
  setMessages: (messages: Message[]) => {
    set((prev) => ({
      ...prev,
      messages: messages,
    }));
  },
});
