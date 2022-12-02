import React, { createContext, ReactNode, useContext } from "react"
import { MetalFlowHandler, metalFlowHandlerInitial, MutableMetalFlowHandler } from "./MetalFlow"

interface DesignerHandler {
    metalFlowHandler: MutableMetalFlowHandler
}

const defaultDesignerHandler = {
    metalFlowHandler: new MutableMetalFlowHandler(metalFlowHandlerInitial)
}


const DesignerCtx: React.Context<DesignerHandler> = createContext<DesignerHandler>(defaultDesignerHandler)

export function useMetalFlow() {
    const ctx = useContext(DesignerCtx);
    const handler: MetalFlowHandler = ctx.metalFlowHandler;
    return handler;
}

export function useAttableMetalFlow() {
    const ctx = useContext(DesignerCtx);
    return ctx.metalFlowHandler;
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