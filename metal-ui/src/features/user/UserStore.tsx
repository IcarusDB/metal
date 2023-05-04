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

import { User } from "../../model/User";
import create from "zustand";
import { subscribeWithSelector } from "zustand/middleware";
import shallow from 'zustand/shallow';

interface Store {
    user: User
}

export interface UserAction {
    getUser: () => User;
    getId: () => string;
    getName: () => string;
    setUser: (user: User) => void;
    setId: (id: string) => void;
    setName: (name: string) => void;
}

export declare type UserStore = Store & UserAction;

export function userActionSelector(store: UserStore) {
    const action: UserAction = {
      getUser: store.getUser,
      getId: store.getId,
      getName: store.getName,
      setUser: store.setUser,
      setId: store.setId,
      setName: store.setName  
    };
    return action;
}

export const useUser = create<UserStore>()(
    subscribeWithSelector((set, get) => ({
        user: {id: "", name: "", roles: []},
        getUser: () => (get().user),
        getId: () => (get().user.id),
        getName: () => (get().user.name),
        setUser: (user: User) => {
            set((prev) => ({
                ...prev,
                user: user
            }));
        },
        setId: (id: string) => {
            set((prev) => ({
                ...prev,
                user: {
                    ...get().user,
                    id: id
                }
            }));
        },
        setName: (name: string) => {
            set((prev) => ({
                ...prev,
                user: {
                    ...get().user,
                    name: name
                }
            }));
        }
    }))
)

export function useUserAction() {
    return useUser(userActionSelector, shallow)
}