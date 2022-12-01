import { useCallback, useEffect, useMemo, useRef } from "react";
import "reactflow/dist/style.css";
import {
    MetalNodeProps,
} from "./MetalView";
import { Alert, IconButton, LinearProgress, Paper, Skeleton, Stack } from "@mui/material";
import { MetalNodeEditor, MetalNodeEditorHandler } from "./MetalNodeEditor";
import { MetalExplorer } from "./explorer/MetalExplorer";
import { Box } from "@mui/system";
import { MetalFlow, MetalFlowHandler } from "./MetalFlow";
import { ProjectProfile, ProjectProfileHandler } from "../project/ProjectProfile";
import { VscSettingsGear } from "react-icons/vsc";
import { Project } from "../../model/Project";
import { MainHandler } from "../main/Main";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../project/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";

export interface DesignerProps {
    id: string,
    name?: string,
    mainHandler?: MainHandler
}

export function Designer(props: DesignerProps) {
    const {id} = props;
    const token: string | null = useAppSelector(state => {
        return tokenSelector(state)
    })
    const {run, status, result, error} = useAsync<Project>()
    const project = result === null? undefined: result;

    const nodeEditorRef = useRef<MetalNodeEditorHandler>(null);
    const metalFlowRef = useRef<MetalFlowHandler>(null);
    const projectProfileRef = useRef<ProjectProfileHandler>(null);

    const isPending = () => (status === State.pending);
    const isFailure = () => (status === State.failure);
    const load = useCallback(()=>{
        if (token !== null) {
            run(getProjectById(token, id))
        }
    }, [id, run, token]);

    

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

    const projectProfile = useMemo(()=>{
        return (
            <ProjectProfile open={false} isCreate={false} project={project} ref={projectProfileRef}/>
        )
    }, [project])


    const nodePropsWrap = useCallback((nodeProps: MetalNodeProps) => ({
        ...nodeProps,
        editorRef: nodeEditorRef
    }), [])

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    useEffect(()=>{
        load();
    }, [load])

    if (project === undefined) {
        return (
            <>
            {isPending() && progress}
            <Skeleton>
                
                {isFailure() && (
                    <Alert severity={"error"}>{"Fail to load project."}</Alert>
                )}
            </Skeleton>
            </>
            
        )
    }

    return (
        <div className="panel">
            {isPending() && progress}
            {isFailure() && <Alert severity={"error"}>{"Fail to load project."}</Alert>}
            <Stack
                direction="row"
                justifyContent="center"
                alignItems="flex-start"
                spacing={2}
                sx={{ height: "100%", width: "100%" }}
            >
                <Box component={Paper} sx={{ height: "100%", width: "75%" }}>
                    <MetalFlow ref={metalFlowRef} nodePropsWrap={nodePropsWrap} />
                </Box>
                <Box component={Paper} sx={{ height: "100%", width: "25%" }}>
                    {explorer}
                </Box>
            </Stack>
            <Paper
                elevation={2}
                sx={{
                    position: "absolute",
                    top: "1vh",
                    left: "1vw",
                }}
            >
                <IconButton
                    onClick={() => {
                        if (projectProfileRef.current !== null) {
                            projectProfileRef.current.open();
                        }
                    }}
                >
                    <VscSettingsGear />
                </IconButton>
            </Paper>
            {nodeEditor}
            {projectProfile}
        </div>
    );
}
