import { createContext, ReactNode, useContext } from "react"
import { createStore, useStore} from "zustand";
import { subscribeWithSelector } from "zustand/middleware";
import { SpecSlice, createSpecSlice } from "./SpecSlice";
import { DesignerActionSlice, createDesignerActionSlice, MetalFlowAction, MetalNodeEditorAction } from "./DesignerActionSlice";
import { Spec } from "../../model/Spec";
import { createDeploySlice, DeploySlice } from "./DeploySlice";
import { BackendStatus } from "../../model/Project";

declare type DesingerStore = DesignerActionSlice & SpecSlice & DeploySlice;

const defaultStore = createStore<DesingerStore>()(
    subscribeWithSelector((set, get) => ({
        ...createDesignerActionSlice(set, get),
        ...createSpecSlice(set, get),
        ...createDeploySlice(set, get),
    }))
)

export const DesignerStoreContext = createContext(defaultStore);

export function useMetalFlow(): [MetalFlowAction, (action: MetalFlowAction) => void] {
    const store = useContext(DesignerStoreContext);
    const [action, setAction] = useStore(store, (state)=>([state.metalFlowAction, state.bindMetalFlowAction]));
    return [action, setAction];
}

export function useMetalNodeEditor(): [MetalNodeEditorAction, (action: MetalNodeEditorAction) => void] {
    const store = useContext(DesignerStoreContext);
    const [action, setAction] = useStore(store, (state)=>([state.metalNodeEditorAction, state.bindMetalNodeEditorAction]));
    return [action, setAction];
}

export function useName(): [
    string | undefined, 
    (name: string) => void,
    (listener: (name: string | undefined, prev: string | undefined) => void) => void
] {
    const store = useContext(DesignerStoreContext);
    const subscribe = (listener: (name: string | undefined, prev: string | undefined) => void ) => {
        store.subscribe(
            state => state.name,
            listener
        );
    }
    return useStore(
        store,
        (state) => ([
            state.name, 
            state.bindName,
            subscribe,
        ])
    );
}

export function usePkgs(): [string[], (pkgs: string[]) => void]{
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.pkgs,
            state.bindPkgs
        ])
    );
}

export function useSpec(): [Spec | undefined, (spec: Spec) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.spec, state.bindSpec])
    );
}

export function usePlatform(): [any | undefined, (platform: any) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.platform, state.bindPlatform])
    );
}

export function useBackendArgs(): [string[], (args: string[]) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.backendArgs, state.bindBackendArgs])
    );
}

export function useProfile(): [
    {name: string | undefined, pkgs: string[], platform: any | undefined, backendArgs: string[]},
    (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => void
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            {
                name: state.name,
                pkgs: state.pkgs,
                platform: state.platform,
                backendArgs: state.backendArgs,
            },
            state.bindProfile
        ])
    )
}

export function useDeployId(): [
    string | undefined,
    (id: string) => void,
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.deployId,
            state.bindDeployId
        ])
    )
}

export function useEpoch(): [
    number | undefined,
    (epoch: number) => void,
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.epoch,
            state.bindEpoch
        ])
    )
}

export function useBackendStatus(): [
    BackendStatus | undefined,
    (status: BackendStatus) => void
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.backendStatus,
            state.bindBackendStatus
        ])
    );
}

export function useDeploy(): [
    {deployId: string | undefined, epoch: number | undefined},
    (deployId?: string, epoch?: number) => void,
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            {
                deployId: state.deployId,
                epoch: state.epoch
            },
            state.bindDeploy
        ])
    )
}



export interface DesignerProviderProps {
    children?: ReactNode
}

export function DesignerProvider(props: DesignerProviderProps) {
    const {children} = props;
    const store = createStore<DesingerStore>()(
        subscribeWithSelector((set, get) => ({
            ...createDesignerActionSlice(set, get),
            ...createSpecSlice(set, get),
            ...createDeploySlice(set, get),
        }))
    )
    return (
        <DesignerStoreContext.Provider value={store}>
            {children}
        </DesignerStoreContext.Provider>
    )
}