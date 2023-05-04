/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Box, IconButton, Paper, Stack } from "@mui/material";
import { ReactNode, useRef } from "react";
import { VscOpenPreview } from "react-icons/vsc";
import { ReactFlowProvider } from "reactflow";
import { useAppSelector } from "../../app/hooks";
import { MainHandler, viewerId } from "../main/Main";
import { ProjectProfileViewer, ProjectProfileViewerHandler } from "../project/ProjectProfile";
import { tokenSelector } from "../user/userSlice";
import { useName, useNameFn } from "./DesignerProvider";
import { MetalFlow } from "./MetalFlow";
import { MetalNodeEditor } from "./MetalNodeEditor";
import { SpecLoader } from "./SpecLoader";

export interface ViewerProps {
    id: string;
    mainHandler?: MainHandler;
    children?: ReactNode;
}

export function Viewer(props: ViewerProps) {
    const { id, mainHandler, children } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const projectProfileViewerRef = useRef<ProjectProfileViewerHandler>(null);
    const [,, onNameChange] = useNameFn();
    onNameChange((name: string | undefined, prev: string | undefined) => {
        if (mainHandler !== undefined && mainHandler.rename !== undefined) {
            mainHandler.rename(viewerId(id), name === undefined ? "?" : name);
        }
    });

    return (
        <div className="panel">
            {children}
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
                        width: "100%",
                    }}
                >
                    <ReactFlowProvider>
                        <MetalFlow
                            isReadOnly={true}
                        />
                    </ReactFlowProvider>
                </Box>
                <SpecLoader token={token} />
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
                        projectProfileViewerRef.current?.open();
                    }}
                >
                    <VscOpenPreview />
                </IconButton>
            </Paper>
            <MetalNodeEditor isReadOnly={true} />
            <ProjectProfileViewer ref={projectProfileViewerRef} />
        </div>
    );
}
