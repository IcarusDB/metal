import { useCallback, useEffect, useMemo, useRef } from "react";
import "reactflow/dist/style.css";
import { MetalNodeProps } from "./MetalView";
import { Alert, IconButton, LinearProgress, Paper, Skeleton, Stack } from "@mui/material";
import { MetalNodeEditor } from "./MetalNodeEditor";
import { MetalExplorer } from "./explorer/MetalExplorer";
import { Box } from "@mui/system";
import { MetalFlow } from "./MetalFlow";
import { ProjectProfile, ProjectProfileHandler } from "../project/ProjectProfile";
import { VscSettingsGear } from "react-icons/vsc";
import { Project } from "../../model/Project";
import { designerId, MainHandler } from "../main/Main";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../project/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { useSpecLoader } from "./SpecLoader";
import { ReactFlowProvider } from "reactflow";
import { useMetalFlow, useMetalNodeEditor } from "./DesignerProvider";
import { IReadOnly } from "../ui/Commons";

export interface DesignerProps extends IReadOnly {
    id: string;
    name?: string;
    mainHandler?: MainHandler;
}

export function Designer(props: DesignerProps) {
    const { id, mainHandler, isReadOnly } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [run, status, result, error] = useAsync<Project>();

    const project = result === null ? undefined : result;
    const specLoader = useSpecLoader(token, project?.spec);

    const projectProfileRef = useRef<ProjectProfileHandler>(null);
    const metalFlowHandler = useMetalFlow();
    const nodeEditorHandler = useMetalNodeEditor();

    const isPending = () => status === State.pending;
    const isFailure = () => status === State.failure;
    const load = useCallback(() => {
        if (token !== null) {
            run(getProjectById(token, id));
        }
    }, [id, run, token]);

    const onAddNode = useCallback(
        (nodeProps: MetalNodeProps) => {
            metalFlowHandler.addNode(nodeProps);
        },
        [metalFlowHandler]
    );

    const explorer = useMemo(() => {
        return <MetalExplorer addNode={onAddNode} restrictPkgs={project?.deploy.pkgs}/>;
    }, [onAddNode, project?.deploy.pkgs]);

    const nodePropsWrap = useCallback(
        (nodeProps: MetalNodeProps) => ({
            ...nodeProps,
            editor: nodeEditorHandler,
        }),
        [nodeEditorHandler]
    );

    useMemo(() => {
        if (mainHandler !== undefined && mainHandler.renameDesigner !== undefined && project !== undefined) {
            mainHandler.renameDesigner(designerId(id, isReadOnly), project.name);
        }
    }, [id, isReadOnly, mainHandler, project])

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    const onReloadProject = (projectId: string) => {
        projectProfileRef.current?.close();
        if (mainHandler !== undefined) {
            if (mainHandler.close !== undefined) {
                mainHandler.close(designerId(id, isReadOnly))
                
                setTimeout(()=>{
                    mainHandler.openDesigner({
                        id: id,
                        isReadOnly: isReadOnly,
                        mainHandler: mainHandler,
                    });
                }, 2000)
            }
        }
    }

    useEffect(() => {
        load();
    }, [load]);

    if (project === undefined) {
        return (
            <>
                {isPending() && progress}
                <Skeleton>
                    {isFailure() && <Alert severity={"error"}>{"Fail to load project."}</Alert>}
                </Skeleton>
            </>
        );
    }

    return (
        <div className="panel">
            {isPending() && progress}
            {isFailure() && <Alert severity={"error"}>{"Fail to load project."}</Alert>}
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
                        width: isReadOnly ? "100%" : "75%",
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
                {!isReadOnly && (
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
            <MetalNodeEditor isReadOnly={isReadOnly} />
            <ProjectProfile
                open={false}
                isCreate={false}
                onFinish={onReloadProject}
                project={project}
                ref={projectProfileRef}
            />
        </div>
    );
}
