import { useCallback, useMemo, useRef } from "react";
import "reactflow/dist/style.css";
import {
    MetalNodeProps,
} from "./MetalView";
import { Paper, Stack } from "@mui/material";
import { MetalNodeEditor, MetalNodeEditorHandler } from "./MetalNodeEditor";
import { MetalExplorer } from "./explorer/MetalExplorer";
import { Box } from "@mui/system";
import { MetalFlow, MetalFlowHandler } from "./MetalFlow";


export function Designer() {
    const nodeEditorRef = useRef<MetalNodeEditorHandler>(null);
    const metalFlowRef = useRef<MetalFlowHandler>(null);

    const onAddNode = useCallback((nodeProps: MetalNodeProps)=>{
        if (metalFlowRef.current !== null) {
            metalFlowRef.current.addNode(nodeProps);
        }
    }, [])

    const explorer = useMemo(()=>{
        return (
            <MetalExplorer addNode={onAddNode}/>
        )
    }, [onAddNode])

    const nodeEditor = useMemo(()=>{
        return (
            <MetalNodeEditor ref={nodeEditorRef} metalFlowRef={metalFlowRef} />
        )
    }, [])


    const nodePropsWrap = useCallback((nodeProps: MetalNodeProps) => ({
        ...nodeProps,
        editorRef: nodeEditorRef
    }), [])

    return (
        <div className="panel">
            <Stack 
                direction="row" 
                justifyContent="center" 
                alignItems="flex-start" 
                spacing={2}
                sx={{height:"100%", width:"100%"}}
            >
                <Box component={Paper} sx={{ height: "100%", width: "75%" }}>
                    <MetalFlow ref={metalFlowRef} nodePropsWrap={nodePropsWrap}/>
                </Box>
                <Box component={Paper} sx={{height:"100%", width:"25%"}}>
                    {explorer}
                </Box>
            </Stack>
            {nodeEditor}
        </div>
    );
}
