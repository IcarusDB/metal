import React, { createContext, ReactNode, useContext } from "react"
import { MetalFlowHandler, metalFlowHandlerInitial, MutableMetalFlowHandler } from "./MetalFlow"
import { MetalNodeEditorHandler, metalNodeEditorHandlerInitial, MutableMetalNodeEditorHandler } from "./MetalNodeEditor"


interface DesignerHandler {
    metalFlowHandler: MutableMetalFlowHandler,
    metalNodeEditorHandler: MutableMetalNodeEditorHandler,
}

const defaultDesignerHandler: DesignerHandler = {
    metalFlowHandler: new MutableMetalFlowHandler(metalFlowHandlerInitial),
    metalNodeEditorHandler: new MutableMetalNodeEditorHandler(metalNodeEditorHandlerInitial),
}


const DesignerCtx: React.Context<DesignerHandler> = createContext<DesignerHandler>(defaultDesignerHandler)

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
    return (
        <DesignerCtx.Provider value={{...defaultDesignerHandler}}>
            {children}
        </DesignerCtx.Provider>
    )
}