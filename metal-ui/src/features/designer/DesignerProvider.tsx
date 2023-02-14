import { createContext, ReactNode, useContext } from "react"
import { createStore, useStore} from "zustand";
import { subscribeWithSelector, devtools } from "zustand/middleware";
import shallow from 'zustand/shallow';
import { SpecSlice, createSpecSlice } from "./SpecSlice";
import { DesignerActionSlice, createDesignerActionSlice, MetalFlowAction, MetalNodeEditorAction, HotNode } from "./DesignerActionSlice";
import { Spec } from "../../model/Spec";
import { createDeploySlice, DeploySlice } from "./DeploySlice";
import { BackendStatus } from "../../model/Project";
import { Exec } from "../../model/Exec";
import { SpecFlow } from "./SpecLoader";

declare type Subscribe<S> = (listener: (s: S | undefined, prev: S | undefined) => void) => () => void;
declare type IChangeFns <S> = [
    (s: S) => void,
    Subscribe<S>,
];

declare type DesingerStore = DesignerActionSlice & SpecSlice & DeploySlice;

const defaultStore = createStore<DesingerStore>()(
    subscribeWithSelector((set, get) => ({
        ...createDesignerActionSlice(set, get),
        ...createSpecSlice(set, get),
        ...createDeploySlice(set, get),
    }))
);

export const DesignerStoreContext = createContext(defaultStore);

export function useFlowPending(): [boolean, 
    (value: boolean) => void,
    Subscribe<boolean>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<boolean> = (listener) => (
        store.subscribe(state => state.isFlowPending, listener)
    );
    return useStore(
        store,
        (state) => ([
            state.isFlowPending,
            state.bindFlowPending,
            sub
        ]),
        shallow
    )
}   

export function useFlowPendingFn(): IChangeFns<boolean> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<boolean> = (listener) => (
        store.subscribe(state => state.isFlowPending, listener)
    );
    return useStore(
        store,
        (state) => ([
            state.bindFlowPending,
            sub
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

export function useModifyFn(): IChangeFns<boolean> {
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
            state.bindModify,
            sub
        ]),
        shallow
    )
}


export function useMetalFlow(): [
    MetalFlowAction, 
    (action: MetalFlowAction) => void,
    Subscribe<MetalFlowAction>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MetalFlowAction> = (listener) => (
        store.subscribe(state => state.metalFlowAction, listener)
    );

    return useStore(
        store, 
        (state)=>([state.metalFlowAction, state.bindMetalFlowAction, sub]),
        shallow
    );
}

export function useMetalFlowFn(): IChangeFns<MetalFlowAction>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MetalFlowAction> = (listener) => (
        store.subscribe(state => state.metalFlowAction, listener)
    );

    return useStore(
        store, 
        (state)=>([state.bindMetalFlowAction, sub]),
        shallow
    );
}

export function useMetalNodeEditor(): [
    MetalNodeEditorAction, 
    (action: MetalNodeEditorAction) => void,
    Subscribe<MetalNodeEditorAction>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MetalNodeEditorAction> = (listener) => (
        store.subscribe(state => state.metalNodeEditorAction, listener)
    );
    return useStore(
        store, 
        (state)=>([
            state.metalNodeEditorAction, 
            state.bindMetalNodeEditorAction,
            sub
        ]),
        shallow
    );
}

export function useMetalNodeEditorFn(): IChangeFns<MetalNodeEditorAction>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MetalNodeEditorAction> = (listener) => (
        store.subscribe(state => state.metalNodeEditorAction, listener)
    );
    return useStore(
        store, 
        (state)=>([
            state.bindMetalNodeEditorAction,
            sub
        ]),
        shallow
    );
}


export function useName(): [
    string | undefined, 
    (name: string) => void,
    Subscribe<string>
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

export function useNameFn(): IChangeFns<string>{
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
            state.bindName,
            subscribe,
        ]),
        shallow
    );
}

export function usePkgs(): [string[], (pkgs: string[]) => void, Subscribe<string[]>]{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string[]> = (listener) => (
        store.subscribe(
            state => state.pkgs,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.pkgs,
            state.bindPkgs,
            sub
        ]),
        shallow
    );
}

export function usePkgsFn(): IChangeFns<string[]>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string[]> = (listener) => (
        store.subscribe(
            state => state.pkgs,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.bindPkgs,
            sub
        ]),
        shallow
    );
}


export function useSpec(): [Spec | undefined, (spec: Spec) => void, Subscribe<Spec>] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<Spec> = (listener) => (
        store.subscribe(
            state => state.spec,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.spec, state.bindSpec, sub]),
        shallow
    );
}


export function useSpecFn(): IChangeFns<Spec> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<Spec> = (listener) => (
        store.subscribe(
            state => state.spec,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.bindSpec, sub]),
        shallow
    );
}


export function useSpecFlow(): [
    SpecFlow | undefined, 
    (flow: SpecFlow) => void,
    Subscribe<SpecFlow>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<SpecFlow> = (listener) => (
        store.subscribe(
            state => state.flow,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.flow, state.bindFlow, sub]),
        shallow
    );
}

export function useSpecFlowFn(): IChangeFns<SpecFlow>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<SpecFlow> = (listener) => (
        store.subscribe(
            state => state.flow,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.bindFlow, sub]),
        shallow
    );
}

export function usePlatform(): [
    any | undefined, 
    (platform: any) => void,
    Subscribe<any>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<any> = (listener) => (
        store.subscribe(
            state => state.platform,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.platform, state.bindPlatform, sub]),
        shallow
    );
}

export function usePlatformFn(): IChangeFns<any>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<any> = (listener) => (
        store.subscribe(
            state => state.platform,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.bindPlatform, sub]),
        shallow
    );
}

export function useBackendArgs(): [
    string[], 
    (args: string[]) => void,
    Subscribe<string[]>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string[]> = (listener) => (
        store.subscribe(
            state => state.backendArgs,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.backendArgs, state.bindBackendArgs, sub]),
        shallow
    );
}

export function useBackendArgsFn(): IChangeFns<string[]>{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string[]> = (listener) => (
        store.subscribe(
            state => state.backendArgs,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([state.bindBackendArgs, sub]),
        shallow
    );
}

type MixedProfile = {
    name?: string,
    pkgs: string[],
    platform?: any,
    backendArgs: string[]
};

export function useProfile(): [
    {name: string | undefined, pkgs: string[], platform: any | undefined, backendArgs: string[]},
    (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => void,
    Subscribe<MixedProfile>

] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MixedProfile> = (listener) => (
        store.subscribe(
            state => ({
                name: state.name,
                pkgs: state.pkgs,
                platform: state.platform,
                backendArgs: state.backendArgs,
            })
        )
    )
    return useStore(
        store,
        (state) => ([
            {
                name: state.name,
                pkgs: state.pkgs,
                platform: state.platform,
                backendArgs: state.backendArgs,
            },
            state.bindProfile,
            sub
        ]),
        shallow
    )
}

export function useProfileFn(): [
    (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => void,
    Subscribe<MixedProfile>
]{
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<MixedProfile> = (listener) => (
        store.subscribe(
            state => ({
                name: state.name,
                pkgs: state.pkgs,
                platform: state.platform,
                backendArgs: state.backendArgs,
            })
        )
    )
    return useStore(
        store,
        (state) => ([
            state.bindProfile,
            sub
        ]),
        shallow
    )
}



export function useHotNodes(): [
    HotNode[],
    (hotNodes: HotNode[]) => void,
    Subscribe<HotNode[]>] {
    const store = useContext(DesignerStoreContext);
    const subscribe: Subscribe<HotNode[]> =  (listener) => {
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

export function useHotNodesFn(): IChangeFns<HotNode[]> {
    const store = useContext(DesignerStoreContext);
    const subscribe: Subscribe<HotNode[]> =  (listener) => {
        return store.subscribe(
            state => state.hotNodes,
            listener
        )
    } 
    return useStore(
        store,
        (state) => ([
            state.bindHotNodes,
            subscribe
        ]),
        shallow
    )
}


export function useDeployId(): [
    string | undefined,
    (id: string) => void,
    Subscribe<string>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string> = (listener) => (
        store.subscribe(
            state => state.deployId,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.deployId,
            state.bindDeployId,
            sub
        ]),
        shallow
    )
}

export function useDeployIdFn(): IChangeFns<string> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string> = (listener) => (
        store.subscribe(
            state => state.deployId,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.bindDeployId,
            sub
        ]),
        shallow
    )
}


export function useEpoch(): [
    number | undefined,
    (epoch: number) => void,
    Subscribe<number>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<number> = (listener) => (
        store.subscribe(
            state => state.epoch,
            listener,
        )
    )
    return useStore(
        store,
        (state) => ([
            state.epoch,
            state.bindEpoch,
            sub
        ]),
        shallow
    )
}

export function useEpochFn(): IChangeFns<number> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<number> = (listener) => (
        store.subscribe(
            state => state.epoch,
            listener,
        )
    )
    return useStore(
        store,
        (state) => ([
            state.bindEpoch,
            sub
        ]),
        shallow
    )
}


export function useBackendStatus(): [
    BackendStatus | undefined,
    (status: BackendStatus) => void,
    Subscribe<BackendStatus>
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

export function useBackendStatusFn(): IChangeFns<BackendStatus>{
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
    Subscribe<Exec>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<Exec> = (listener) => (
        store.subscribe(
            state => state.exec,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.exec,
            state.bindExec,
            sub
        ]),
        shallow
    )
}

export function useExecInfoFn(): IChangeFns<Exec | undefined> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<Exec | undefined> = (listener) => (
        store.subscribe(
            state => state.exec,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.bindExec,
            sub
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