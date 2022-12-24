import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import "reactflow/dist/style.css";
import { MetalNodeProps } from "./MetalView";
import { Alert, IconButton, LinearProgress, Paper, Skeleton, Stack } from "@mui/material";
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
import { Project } from "../../model/Project";
import { designerId, MainHandler } from "../main/Main";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../../api/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { useSpecLoader } from "./SpecLoader";
import { ReactFlowProvider } from "reactflow";
import { useBackendArgs, useMetalFlow, useMetalNodeEditor, useName, usePkgs, usePlatform, useSpec } from "./DesignerProvider";
import { IReadOnly } from "../ui/Commons";
import { BackendPanel, BackendPanelHandler } from "./BackendPanel";

function useProjectLoader(token: string | null, id: string) {
    const [run, status, result, error] = useAsync<Project>();
    const [, setName] = useName();
    const [, setPkgs] = usePkgs();
    const [, setSpec] = useSpec();
    const [, setPlatform] = usePlatform();
    const [, setBackendArgs] = useBackendArgs();

    useEffect(() => {
        if (token === null || id.trim() === "")  {
            return;
        }
        run(
            getProjectById(token, id).then(proj => {
                setSpec(proj.spec);
                setPkgs(proj.deploy.pkgs);
                setName(proj.name);
                setBackendArgs(proj.deploy.backend.args);
                setPlatform(proj.deploy.platform);
                return proj;
            })
        );
    }, [id, run, setBackendArgs, setName, setPkgs, setPlatform, setSpec, token])

    return [status, error]
}

export interface ProjectLoaderProps {
    token: string | null,
    id: string
}

export function ProjectLoader(props: ProjectLoaderProps) {
    const {token, id} = props;
    const [loadStatus, loadError] = useProjectLoader(token, id);

    const isPending = () => loadStatus === State.pending;
    const isFailure = () => loadStatus === State.failure;

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    return (
        <>
            {isPending() && progress}
            {isFailure() && <Alert severity={"error"}>{"Fail to load project."}</Alert>}
        </>
    )
}

export interface DesignerProps extends IReadOnly {
    id: string;
    mainHandler?: MainHandler;
}

export function Designer(props: DesignerProps) {
    const { id, mainHandler, isReadOnly } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [isOpenExplorer, setOpenExplorer] = useState(isReadOnly ? false : true);

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
        if (isReadOnly !== undefined && isReadOnly === false) {
            return;
        }
        setOpenExplorer(!isOpenExplorer);
    };

    onNameChange((name: string | undefined, prev: string | undefined) => {
        if (mainHandler !== undefined && mainHandler.renameDesigner !== undefined) {
            mainHandler.renameDesigner(designerId(id, isReadOnly), name === undefined? "?": name);
        }
    })

    const onAddNode = useCallback(
        (nodeProps: MetalNodeProps) => {
            metalFlowAction.addNode(nodeProps);
        },
        [metalFlowAction]
    );

    const explorer = useMemo(() => {
        return <MetalExplorer addNode={onAddNode} restrictPkgs={pkgs} />;
    }, [onAddNode, pkgs]);

    const nodePropsWrap = useCallback(
        (nodeProps: MetalNodeProps) => ({
            ...nodeProps,
            editor: nodeEditorAction,
        }),
        [nodeEditorAction]
    );

    const onProfileFinish = (projectId: string) => {
        projectProfileRef.current?.close();
        // if (mainHandler !== undefined) {
        //     if (mainHandler.close !== undefined) {
        //         mainHandler.close(designerId(id, isReadOnly));

        //         setTimeout(() => {
        //             mainHandler.openDesigner({
        //                 id: id,
        //                 isReadOnly: isReadOnly,
        //                 mainHandler: mainHandler,
        //             });
        //         }, 2000);
        //     }
        // }
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
                            isReadOnly={isReadOnly}
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
                        {explorer}
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
                    {!isReadOnly && (
                        <IconButton
                            onClick={() => {
                                if (projectProfileRef.current !== null) {
                                    projectProfileRef.current.open();
                                }
                            }}
                        >
                            <VscSettingsGear />
                        </IconButton>
                    )}

                    <IconButton
                        onClick={() => {
                            projectProfileViewerRef.current?.open(id);
                        }}
                    >
                        <VscOpenPreview />
                    </IconButton>

                    {!isReadOnly && (
                        <IconButton onClick={onSwitchExplorer}>
                            <VscExtensions />
                        </IconButton>
                    )}
                </div>
                {/* <BackendPanel deployId={project.deploy.id} currentSpec={()=>{return metalFlowAction.export()}} ref={backendPanelRef}/> */}
            </Paper>
            <MetalNodeEditor isReadOnly={isReadOnly} />
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
