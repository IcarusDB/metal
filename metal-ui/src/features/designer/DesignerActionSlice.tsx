import { Node } from "reactflow";
import { emptySpec, Spec } from "../../model/Spec";
import { MetalNodeProps, MetalNodeState } from "./MetalView";
import { SpecFlow } from "./SpecLoader";
import { StoreApi } from "zustand";

export declare type HotNode = [string, MetalNodeState, string | undefined];

export interface MetalFlowAction {
    allNodes: () => Node<MetalNodeProps>[];
    inputs: (id: string) => Node<MetalNodeProps>[];
    outputs: (id: string) => Node<MetalNodeProps>[];
    addNode: (nodeProps: MetalNodeProps) => void;
    load: (newFlow: SpecFlow | undefined) => void;
    export: () => Spec;
}

export const initialMetalFlowAction: MetalFlowAction = {
    allNodes: () => [],
    inputs: (id: string) => [],
    outputs: (id: string) => [],
    addNode: (nodeProps: MetalNodeProps) => { },
    load: (newFlow: SpecFlow | undefined) => { },
    export: () => (emptySpec()),
};

export interface MetalNodeEditorAction {
    load: (props: MetalNodeProps) => void;
    close: () => void;
}

export const initialMetalNodeEditorAction: MetalNodeEditorAction = {
    load: (props: MetalNodeProps) => { },
    close: () => { },
};
export interface DesignerActionSlice {
    isFlowPending: boolean;
    isModify: boolean;
    metalFlowAction: MetalFlowAction;
    metalNodeEditorAction: MetalNodeEditorAction;
    getFlowPending: () => boolean;
    setFlowPending: (value: boolean) => void;
    getModify: () => boolean;
    setModify: (isModify: boolean) => void;
    getMetalFlowAction: () => MetalFlowAction;
    setMetalFlowAction: (action: MetalFlowAction) => void;
    getMetalNodeEditorAction: () => MetalNodeEditorAction;
    setMetalNodeEditorAction: (action: MetalNodeEditorAction) => void;
    name?: string,
    getName: () => string | undefined;
    setName: (name: string) => void;
    pkgs: string[],
    getPkgs: () => string[];
    setPkgs: (pkgs: string[]) => void;
    platform?: any,
    getPlatform: () => any;
    setPlatform: (platform: any) => void;
    backendArgs: string[];
    getBackendArgs: () => string[];
    setBackendArgs: (args: string[]) => void;
    getProfile: () => {name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]};
    setProfile: (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => void;
    hotNodes: HotNode[],
    getHotNode: () => HotNode[];
    setHotNodes: (hotNodes: HotNode[]) => void;
}

export const createDesignerActionSlice = (
    set: StoreApi<DesignerActionSlice>["setState"],
    get: StoreApi<DesignerActionSlice>["getState"]
): DesignerActionSlice => ({
    isFlowPending: false,
    isModify: true,
    metalFlowAction: initialMetalFlowAction,
    metalNodeEditorAction: initialMetalNodeEditorAction,
    getFlowPending: () => (get().isFlowPending),
    setFlowPending: (value: boolean) => {
        set((prev) => ({
            ...prev,
            isFlowPending: value,
        }));
    },
    getModify: () => (get().isModify),
    setModify: (isModify: boolean) => {
        set((prev) => ({
            ...prev,
            isModify: isModify,
        }))
    },
    getMetalFlowAction: () => (get().metalFlowAction),
    setMetalFlowAction: (action: MetalFlowAction) => {
        set((prev) => ({
            ...prev,
            metalFlowAction: action,
        }));
    },
    getMetalNodeEditorAction: () => (get().metalNodeEditorAction),
    setMetalNodeEditorAction: (action: MetalNodeEditorAction) => {
        set((prev) => ({
            ...prev,
            metalNodeEditorAction: action,
        }));
    },
    getName: () => (get().name),
    setName: (name: string) => {
        set((prev) => ({
            ...prev,
            name: name
        }));
    },
    pkgs: [],
    getPkgs: () => (get().pkgs),
    setPkgs: (pkgs: string[]) => {
        set((prev) => ({
            ...prev,
            pkgs: pkgs
        }));
    },
    getPlatform: () => (get().platform),
    setPlatform: (platform: any) => {
        set((prev) => ({
            ...prev,
            platform: platform
        }));
    },
    backendArgs: [],
    getBackendArgs: () => (get().backendArgs),
    setBackendArgs: (args: string[]) => {
        set((prev) => ({
            ...prev,
            backendArgs: args
        }));
    },
    getProfile: () => ({name: get().name, pkgs: get().pkgs, platform: get().platform, backendArgs: get().backendArgs}),
    setProfile: (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => {
        set((prev) => ({
            ...prev,
            name,
            pkgs,
            platform,
            backendArgs,
        }));
    },
    hotNodes: [],
    getHotNode: () => (get().hotNodes),
    setHotNodes: (hotNodes: HotNode[]) => {
        set((prev) => ({
            ...prev,
            hotNodes
        }))
    },
});
