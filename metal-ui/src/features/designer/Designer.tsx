import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import "reactflow/dist/style.css";
import { IconButton, Paper, Stack } from "@mui/material";
import { MetalNodeEditor } from "./MetalNodeEditor";
import { MetalExplorerWrapper } from "./explorer/MetalExplorer";
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
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { SpecLoader } from "./SpecLoader";
import { ReactFlowProvider } from "reactflow";
import { useNameFn } from "./DesignerProvider";
import { ProjectLoader } from "./ProjectLoader";
import { BackendBar } from "./backend/BackendBar";
import { SaveSpec } from "./SaveSpec";

export interface DesignerProps {
    id: string;
    mainHandler?: MainHandler;
}

export function Designer(props: DesignerProps) {
    console.log("Desiger");
    const { id, mainHandler } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [isOpenExplorer, setOpenExplorer] = useState(true);

    const [,, onNameChange] = useNameFn();

    const projectProfileRef = useRef<ProjectProfileHandler>(null);
    const projectProfileViewerRef = useRef<ProjectProfileViewerHandler>(null);
    
    const onSwitchExplorer = useCallback(() => {
        setOpenExplorer(!isOpenExplorer);
    }, [isOpenExplorer]);

    const onProfileFinish = (projectId: string) => {
        projectProfileRef.current?.close();
    };

    const editor = useMemo(()=>(<MetalNodeEditor />), []);

    useEffect(() => {
        const unsub = onNameChange((name: string | undefined, prev: string | undefined) => {
            if (mainHandler !== undefined && mainHandler.rename !== undefined) {
                mainHandler.rename(designerId(id), name === undefined ? "?" : name);
            }
        });
        return unsub;
    }, [id, mainHandler, onNameChange]);

    return (
        <div className="panel">
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
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "space-between",
                    }}
                >
                    <ReactFlowProvider>
                        <MetalFlow />
                    </ReactFlowProvider>
                    <BackendBar id={id} />
                </Box>
                <ProjectLoader token={token} id={id} />
                <SpecLoader token={token} />
                {isOpenExplorer && (
                    <Box
                        component={Paper}
                        sx={{
                            height: "100%",
                            width: "25%",
                        }}
                    >
                        <MetalExplorerWrapper />
                    </Box>
                )}
            </Stack>
            <Paper
                elevation={2}
                sx={{
                    position: "absolute",
                    top: "1vh",
                    left: "1vw",
                    padding: "0.5em",
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "flex-start",
                    justifyContent: "flex-start",
                }}
            >
                <IconButton
                    size="small"
                    sx={{
                        borderRadius: "0px",
                    }}
                    onClick={() => {
                        if (projectProfileRef.current !== null) {
                            projectProfileRef.current.open();
                        }
                    }}
                >
                    <VscSettingsGear />
                </IconButton>

                <IconButton
                    size="small"
                    sx={{
                        borderRadius: "0px",
                    }}
                    onClick={() => {
                        projectProfileViewerRef.current?.open();
                    }}
                >
                    <VscOpenPreview />
                </IconButton>

                <IconButton
                    size="small"
                    sx={{
                        borderRadius: "0px",
                    }}
                    onClick={onSwitchExplorer}
                >
                    <VscExtensions />
                </IconButton>
                <SaveSpec token={token} id={id} />
            </Paper>
            {editor}
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
