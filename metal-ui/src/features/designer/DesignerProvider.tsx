import { createContext, ReactNode, useContext } from "react"
import { createStore, useStore} from "zustand";
import { subscribeWithSelector, devtools } from "zustand/middleware";
import shallow from 'zustand/shallow';
import { SpecSlice, createSpecSlice } from "./SpecSlice";
import { DesignerActionSlice, createDesignerActionSlice, MetalFlowAction, MetalNodeEditorAction } from "./DesignerActionSlice";
import { Spec } from "../../model/Spec";
import { createDeploySlice, DeploySlice } from "./DeploySlice";
import { BackendStatus } from "../../model/Project";
import { AnalysisResponse } from "../../api/ProjectApi";
import { MetalNodeState } from "./MetalView";
import { Exec } from "../../model/Exec";
import { SpecFlow } from "./SpecLoader";

declare type DesingerStore = DesignerActionSlice & SpecSlice & DeploySlice;

const defaultStore = createStore<DesingerStore>()(
    subscribeWithSelector((set, get) => ({
        ...createDesignerActionSlice(set, get),
        ...createSpecSlice(set, get),
        ...createDeploySlice(set, get),
    }))
);

export const DesignerStoreContext = createContext(defaultStore);

export function useFlowPending(): [boolean, (value: boolean) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.isFlowPending,
            state.bindFlowPending,
        ]),
        shallow
    )
}

export function useModify(): [boolean, (isModify: boolean)=>void, (listener: (status: boolean, prev: boolean) => void) => () => void] {
    const store = useContext(DesignerStoreContext);
    const sub = (listener: (status: boolean, prev: boolean) => void) => {
        return store.subscribe(
            state => state.isModify,
            listener
        )
    };

    return useStore(
        store,
        (state) => ([
            state.isModify,
            state.bindModify,
            sub
        ]),
        shallow
    )
}

export function useMetalFlow(): [MetalFlowAction, (action: MetalFlowAction) => void] {
    const store = useContext(DesignerStoreContext);
    const [action, setAction] = useStore(
        store, 
        (state)=>([state.metalFlowAction, state.bindMetalFlowAction]),
        shallow
    );
    return [action, setAction];
}

export function useMetalNodeEditor(): [MetalNodeEditorAction, (action: MetalNodeEditorAction) => void] {
    const store = useContext(DesignerStoreContext);
    const [action, setAction] = useStore(
        store, 
        (state)=>([state.metalNodeEditorAction, state.bindMetalNodeEditorAction]),
        shallow
    );
    return [action, setAction];
}

export function useName(): [
    string | undefined, 
    (name: string) => void,
    (listener: (name: string | undefined, prev: string | undefined) => void) => () => void
] {
    const store = useContext(DesignerStoreContext);
    const subscribe = (listener: (name: string | undefined, prev: string | undefined) => void ) => {
        return store.subscribe(
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
        ]),
        shallow
    );
}

export function usePkgs(): [string[], (pkgs: string[]) => void]{
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.pkgs,
            state.bindPkgs
        ]),
        shallow
    );
}

export function useSpec(): [Spec | undefined, (spec: Spec) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.spec, state.bindSpec]),
        shallow
    );
}

export function useSpecFlow(): [SpecFlow | undefined, (flow: SpecFlow) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.flow, state.bindFlow]),
        shallow
    );
}

export function usePlatform(): [any | undefined, (platform: any) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.platform, state.bindPlatform]),
        shallow
    );
}

export function useBackendArgs(): [string[], (args: string[]) => void] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([state.backendArgs, state.bindBackendArgs]),
        shallow
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
        ]),
        shallow
    )
}

export function useHotNodes(): [
    [string, MetalNodeState][],
    (hotNodes: [string, MetalNodeState][]) => void,
    (listener: (hotNodes: [string, MetalNodeState][] | undefined, prev: [string, MetalNodeState][] | undefined) => void) => () => void,
] {
    const store = useContext(DesignerStoreContext);
    const subscribe = (listener:  (hotNodes: [string, MetalNodeState][] | undefined, prev: [string, MetalNodeState][] | undefined) => void) => {
        return store.subscribe(
            state => state.hotNodes,
            listener
        )
    } 
    return useStore(
        store,
        (state) => ([
            state.hotNodes,
            state.bindHotNodes,
            subscribe
        ]),
        shallow
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
        ]),
        shallow
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
        ]),
        shallow
    )
}

export function useBackendStatus(): [
    BackendStatus | undefined,
    (status: BackendStatus) => void,
    (listener: (status: BackendStatus | undefined, prev: BackendStatus | undefined) => void) => () => void
] {
    const store = useContext(DesignerStoreContext);
    const sub = (listener: (status: BackendStatus | undefined, prev: BackendStatus | undefined) => void) => {
        return store.subscribe(
            state => state.backendStatus,
            listener
        );
    }
    
    return useStore(
        store,
        (state) => ([
            state.backendStatus,
            state.bindBackendStatus,
            sub
        ]),
        shallow
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
        ]),
        shallow
    )
}

export function useExecInfo(): [
    Exec | undefined,
    (exec: Exec | undefined) => void,
] {
    const store = useContext(DesignerStoreContext);
    return useStore(
        store,
        (state) => ([
            state.exec,
            state.bindExec
        ]),
        shallow
    )
}



export interface DesignerProviderProps {
    children?: ReactNode
}

export function DesignerProvider(props: DesignerProviderProps) {
    console.log("Designer Provider.");
    const {children} = props;
    const store = createStore<DesingerStore>()(
        devtools(subscribeWithSelector((set, get) => ({
            ...createDesignerActionSlice(set, get),
            ...createSpecSlice(set, get),
            ...createDeploySlice(set, get),
        })), {
            name: "zustand",
            enabled: true,
        })
    )
    return (
        <DesignerStoreContext.Provider value={store}>
            {children}
        </DesignerStoreContext.Provider>
    )
}