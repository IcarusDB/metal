import _ from "lodash";
import React, { createContext, ReactNode, useContext } from "react"
import { Node } from "reactflow";
import { Mutable } from "../../model/Mutable";
import { emptySpec, Spec } from "../../model/Spec";
import { MetalNodeProps } from "./MetalView";
import { SpecFlow } from "./SpecLoader";


export interface MetalFlowHandler {
    inputs: (id: string) => Node<MetalNodeProps>[];
    outputs: (id: string) => Node<MetalNodeProps>[];
    addNode: (nodeProps: MetalNodeProps) => void;
    load: (newFlow: SpecFlow | undefined) => void;
    export: () => Spec;
}

export const metalFlowHandlerInitial: MetalFlowHandler = {
    inputs: (id: string) => [],
    outputs: (id: string) => [],
    addNode: (nodeProps: MetalNodeProps) => {},
    load: (newFlow: SpecFlow | undefined) => {},
    export: () => (emptySpec()),
};

export class MutableMetalFlowHandler extends Mutable<MetalFlowHandler> implements MetalFlowHandler {
    inputs(id: string) {
        return this.get().inputs(id);
    }

    outputs(id: string) {
        return this.get().outputs(id);
    }

    addNode(nodeProps: MetalNodeProps) {
        this.get().addNode(nodeProps);
    }

    load(newFlow: SpecFlow | undefined) {
        this.get().load(newFlow);
    }

    export() {
        return this.get().export();
    }
}

export interface MetalNodeEditorHandler {
    load: (props: MetalNodeProps) => void;
    close: () => void;
}

export const metalNodeEditorHandlerInitial: MetalNodeEditorHandler = {
    load: (props: MetalNodeProps) => {},
    close: () => {},
};

export class MutableMetalNodeEditorHandler extends Mutable<MetalNodeEditorHandler> implements MetalNodeEditorHandler {
    load(props: MetalNodeProps) {
        this.get().load(props);
    }
    
    close() {
        this.get().close();
    }
}

export interface DesignerHandler {
    metalFlowHandler: MutableMetalFlowHandler,
    metalNodeEditorHandler: MutableMetalNodeEditorHandler,
}

export const defaultDesignerHandler: DesignerHandler = {
    metalFlowHandler: new MutableMetalFlowHandler(metalFlowHandlerInitial),
    metalNodeEditorHandler: new MutableMetalNodeEditorHandler(metalNodeEditorHandlerInitial),
}
 

export const DesignerCtx: React.Context<DesignerHandler> = createContext<DesignerHandler>(defaultDesignerHandler)

export function useMetalFlow() {
    const ctx = useContext(DesignerCtx);
    const handler: MetalFlowHandler = ctx.metalFlowHandler;
    return handler;
}

export function useMutableMetalFlow() {
    const ctx = useContext(DesignerCtx);
    return ctx.metalFlowHandler;
}

export function useMetalNodeEditor() {
    const ctx = useContext(DesignerCtx);
    const handler: MetalNodeEditorHandler = ctx.metalNodeEditorHandler;
    return handler;
}

export function useMutableMetalNodeEditor() {
    const ctx = useContext(DesignerCtx);
    return ctx.metalNodeEditorHandler;
}

export interface DesignerProviderProps {
    children?: ReactNode
}

export function DesignerProvider(props: DesignerProviderProps) {
    const {children} = props;
    const designerHandler: DesignerHandler = {
        metalFlowHandler: new MutableMetalFlowHandler(_.clone(metalFlowHandlerInitial)),
        metalNodeEditorHandler: new MutableMetalNodeEditorHandler(_.clone(metalNodeEditorHandlerInitial))
    }
    return (
        <DesignerCtx.Provider value={{...designerHandler}}>
            {children}
        </DesignerCtx.Provider>
    )
}