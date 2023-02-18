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