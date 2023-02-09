import { Node } from "reactflow";
import { emptySpec, Spec } from "../../model/Spec";
import { MetalNodeProps, MetalNodeState } from "./MetalView";
import { SpecFlow } from "./SpecLoader";
import { StoreApi } from "zustand";


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
    bindFlowPending: (value: boolean) => void;
    bindModify: (isModify: boolean) => void;
    bindMetalFlowAction: (action: MetalFlowAction) => void;
    bindMetalNodeEditorAction: (action: MetalNodeEditorAction) => void;
    name?: string,
    bindName: (name: string) => void;
    pkgs: string[],
    bindPkgs: (pkgs: string[]) => void;
    platform?: any,
    bindPlatform: (platform: any) => void;
    backendArgs: string[],
    bindBackendArgs: (args: string[]) => void;
    bindProfile: (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => void;
    hotNodes: [string, MetalNodeState][],
    bindHotNodes: (hotNodes: [string, MetalNodeState][]) => void;
}

export const createDesignerActionSlice = (
    set: StoreApi<DesignerActionSlice>["setState"],
    get: StoreApi<DesignerActionSlice>["getState"]
): DesignerActionSlice => ({
    isFlowPending: false,
    isModify: true,
    metalFlowAction: initialMetalFlowAction,
    metalNodeEditorAction: initialMetalNodeEditorAction,
    bindFlowPending: (value: boolean) => {
        set((prev) => ({
            ...prev,
            isFlowPending: value,
        }));
    },
    bindModify: (isModify: boolean) => {
        set((prev) => ({
            ...prev,
            isModify: isModify,
        }))
    },
    bindMetalFlowAction: (action: MetalFlowAction) => {
        set((prev) => ({
            ...prev,
            metalFlowAction: action,
        }));
    },
    bindMetalNodeEditorAction: (action: MetalNodeEditorAction) => {
        set((prev) => ({
            ...prev,
            metalNodeEditorAction: action,
        }));
    },
    bindName: (name: string) => {
        set((prev) => ({
            name: name
        }));
    },
    pkgs: [],
    bindPkgs: (pkgs: string[]) => {
        set((prev) => ({
            pkgs: pkgs
        }));
    },
    bindPlatform: (platform: any) => {
        set((prev) => ({
            platform: platform
        }));
    },
    backendArgs: [],
    bindBackendArgs: (args: string[]) => {
        set((prev) => ({
            backendArgs: args
        }));
    },
    bindProfile: (name?: string, pkgs?: string[], platform?: any, backendArgs?: string[]) => {
        set((prev) => ({
            name,
            pkgs,
            platform,
            backendArgs,
        }));
    },
    hotNodes: [],
    bindHotNodes: (hotNodes: [string, MetalNodeState][]) => {
        set((prev) => ({
            hotNodes
        }))
    },
});
