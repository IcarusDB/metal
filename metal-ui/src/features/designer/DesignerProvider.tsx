import { createContext, ReactNode, useContext } from "react"
import { Node } from "reactflow";
import { emptySpec, Spec } from "../../model/Spec";
import { MetalNodeProps } from "./MetalView";
import { SpecFlow } from "./SpecLoader";
import { createStore, useStore} from "zustand";
import { subscribeWithSelector } from "zustand/middleware";

export interface MetalFlowAction {
    inputs: (id: string) => Node<MetalNodeProps>[];
    outputs: (id: string) => Node<MetalNodeProps>[];
    addNode: (nodeProps: MetalNodeProps) => void;
    load: (newFlow: SpecFlow | undefined) => void;
    export: () => Spec;
}

export const initialMetalFlowAction: MetalFlowAction = {
    inputs: (id: string) => [],
    outputs: (id: string) => [],
    addNode: (nodeProps: MetalNodeProps) => {},
    load: (newFlow: SpecFlow | undefined) => {},
    export: () => (emptySpec()),
};

export interface MetalNodeEditorAction {
    load: (props: MetalNodeProps) => void;
    close: () => void;
}

export const initialMetalNodeEditorAction: MetalNodeEditorAction = {
    load: (props: MetalNodeProps) => {},
    close: () => {},
};

interface DesignerAction {
    metalFlowAction: MetalFlowAction,
    metalNodeEditorAction: MetalNodeEditorAction,
    bindMetalFlowAction: (action: MetalFlowAction) => void,
    bindMetalNodeEditorAction: (action: MetalNodeEditorAction) => void,
}

const store = createStore<DesignerAction>()(
    subscribeWithSelector((set, get) => ({
        metalFlowAction: initialMetalFlowAction,
        metalNodeEditorAction: initialMetalNodeEditorAction,
        bindMetalFlowAction: (action: MetalFlowAction) => {
            set((prev) => ({
                ...prev,
                metalFlowAction: action,
            }))
        },
        bindMetalNodeEditorAction: (action: MetalNodeEditorAction) => {
            set((prev) => ({
                ...prev,
                metalNodeEditorAction: action,
            }))
        },
    }))
)

export const DesignerStoreContext = createContext(store);

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

export interface DesignerProviderProps {
    children?: ReactNode
}

export function DesignerProvider(props: DesignerProviderProps) {
    const {children} = props;
    return (
        <DesignerStoreContext.Provider value={store}>
            {children}
        </DesignerStoreContext.Provider>
    )
}