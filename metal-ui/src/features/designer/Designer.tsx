import { useCallback, useMemo, useRef, useState } from "react";
import "reactflow/dist/style.css";
import { MetalNodeProps } from "./MetalView";
import { Alert, IconButton, Paper, Stack } from "@mui/material";
import { MetalNodeEditor } from "./MetalNodeEditor";
import { MetalExplorer } from "./explorer/MetalExplorer";
import { Box } from "@mui/system";
import { MetalFlow } from "./MetalFlow";
import {
    ProjectProfile,
    ProjectProfileHandler,
    ProjectProfileViewer,
    ProjectProfileViewerHandler,
} from "../project/ProjectProfile";
import { VscExtensions, VscOpenPreview, VscSettingsGear } from "react-icons/vsc";
import { designerId, MainHandler } from "../main/Main";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { useSpecLoader } from "./SpecLoader";
import { ReactFlowProvider } from "reactflow";
import { useMetalFlow, useMetalNodeEditor, useName, usePkgs, useSpec } from "./DesignerProvider";
import { BackendPanelHandler } from "./BackendPanel";
import { ProjectLoader } from "./ProjectLoader";

export interface DesignerProps {
    id: string;
    mainHandler?: MainHandler;
}

export function Designer(props: DesignerProps) {
    const { id, mainHandler } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [isOpenExplorer, setOpenExplorer] = useState(true);

    const [,, onNameChange] = useName();
    const [spec] = useSpec();
    const specLoader = useSpecLoader(token, spec);
    const [pkgs, setPkgs] = usePkgs();

    const projectProfileRef = useRef<ProjectProfileHandler>(null);
    const projectProfileViewerRef = useRef<ProjectProfileViewerHandler>(null);
    const backendPanelRef = useRef<BackendPanelHandler>(null);
    const [metalFlowAction] = useMetalFlow();
    const [nodeEditorAction] = useMetalNodeEditor();

    const onSwitchExplorer = () => {
        setOpenExplorer(!isOpenExplorer);
    };

    onNameChange((name: string | undefined, prev: string | undefined) => {
        if (mainHandler !== undefined && mainHandler.renameDesigner !== undefined) {
            mainHandler.renameDesigner(designerId(id), name === undefined? "?": name);
        }
    })

    const onAddNode = useCallback(
        (nodeProps: MetalNodeProps) => {
            metalFlowAction.addNode(nodeProps);
        },
        [metalFlowAction]
    );

    const nodePropsWrap = useCallback(
        (nodeProps: MetalNodeProps) => ({
            ...nodeProps,
            editor: nodeEditorAction,
        }),
        [nodeEditorAction]
    );

    const onProfileFinish = (projectId: string) => {
        projectProfileRef.current?.close();
    };


    return (
        <div className="panel">
            <ProjectLoader token={token} id={id}/>
            {specLoader.status === State.failure && (
                <Alert severity={"error"}>{"Fail to load project spec."}</Alert>
            )}
            <Stack
                direction="row"
                justifyContent="center"
                alignItems="flex-start"
                spacing={2}
                sx={{ height: "100%", width: "100%" }}
            >
                <Box
                    component={Paper}
                    sx={{
                        height: "100%",
                        width: !isOpenExplorer ? "100%" : "75%",
                    }}
                >
                    <ReactFlowProvider>
                        <MetalFlow
                            flow={specLoader.flow === null ? undefined : specLoader.flow}
                            nodePropsWrap={nodePropsWrap}
                        />
                    </ReactFlowProvider>
                </Box>
                {isOpenExplorer && (
                    <Box
                        component={Paper}
                        sx={{
                            height: "100%",
                            width: "25%",
                        }}
                    >
                        <MetalExplorer addNode={onAddNode} restrictPkgs={pkgs} />
                    </Box>
                )}
            </Stack>
            <Paper
                elevation={2}
                sx={{
                    position: "absolute",
                    top: "1vh",
                    left: "1vw",
                    paddingTop: "1em",
                    paddingBottom: "1em",
                    paddingLeft: "1em",
                    paddingRight: "1em",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "flex-start",
                    justifyContent: "flex-start",
                }}
            >
                <div
                    style={{
                        width: "100%",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "flex-start",
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
                  

                    <IconButton
                        onClick={() => {
                            projectProfileViewerRef.current?.open();
                        }}
                    >
                        <VscOpenPreview />
                    </IconButton>

                    <IconButton onClick={onSwitchExplorer}>
                         <VscExtensions />
                    </IconButton>
                </div>
                {/* <BackendPanel deployId={project.deploy.id} currentSpec={()=>{return metalFlowAction.export()}} ref={backendPanelRef}/> */}
            </Paper>
            {<MetalNodeEditor />}
            <ProjectProfile
                open={false}
                isCreate={false}
                onFinish={onProfileFinish}
                id={id}
                ref={projectProfileRef}
            />
            <ProjectProfileViewer ref={projectProfileViewerRef} />
        </div>
    );
}
