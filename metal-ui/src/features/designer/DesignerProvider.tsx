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
    () => S | undefined,
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
            state.setFlowPending,
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
            state.getFlowPending,
            state.setFlowPending,
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
            state.setModify,
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
            state.getModify,
            state.setModify,
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
        (state)=>([state.metalFlowAction, state.setMetalFlowAction, sub]),
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
        (state)=>([state.getMetalFlowAction, state.setMetalFlowAction, sub]),
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
            state.setMetalNodeEditorAction,
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
            state.getMetalNodeEditorAction,
            state.setMetalNodeEditorAction,
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
            state.setName,
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
            state.getName,
            state.setName,
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
            state.setPkgs,
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
            state.getPkgs,
            state.setPkgs,
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
        (state) => ([state.spec, state.setSpec, sub]),
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
        (state) => ([state.getSpec, state.setSpec, sub]),
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
        (state) => ([state.flow, state.setFlow, sub]),
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
        (state) => ([state.getFlow, state.setFlow, sub]),
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
        (state) => ([state.platform, state.setPlatform, sub]),
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
        (state) => ([state.getPlatform, state.setPlatform, sub]),
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
        (state) => ([state.backendArgs, state.setBackendArgs, sub]),
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
        (state) => ([state.getBackendArgs, state.setBackendArgs, sub]),
        shallow
    );
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
            state.setHotNodes,
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
            state.getHotNode,
            state.setHotNodes,
            subscribe
        ]),
        shallow
    )
}

export function useProjectId(): [
    string | undefined,
    (id: string) => void,
    Subscribe<string>
] {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string> = (listener) => (
        store.subscribe(
            state => state.projectId,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.projectId,
            state.setProjectId,
            sub
        ]),
        shallow
    )
}

export function useProjectIdFn(): IChangeFns<string> {
    const store = useContext(DesignerStoreContext);
    const sub: Subscribe<string> = (listener) => (
        store.subscribe(
            state => state.projectId,
            listener
        )
    )
    return useStore(
        store,
        (state) => ([
            state.getProjectId,
            state.setProjectId,
            sub
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
            state.setDeployId,
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
            state.getDeployId,
            state.setDeployId,
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
            state.setEpoch,
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
            state.getEpoch,
            state.setEpoch,
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
            state.setBackendStatus,
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
            state.getBackendStatus,
            state.setBackendStatus,
            sub
        ]),
        shallow
    );
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
            state.setExec,
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
            state.getExec,
            state.setExec,
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