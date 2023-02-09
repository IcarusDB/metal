import { Alert, Button, Grid, IconButton, Paper, Popover, Stack, Typography } from "@mui/material";
import moment from "moment";
import { useCallback, useEffect, useMemo, useState, useTransition } from "react";
import { AiFillThunderbolt, AiOutlineApi, AiOutlineWarning } from "react-icons/ai";
import { HiStop } from "react-icons/hi";
import {
    VscBookmark,
    VscComment,
    VscDebugAltSmall,
    VscDebugDisconnect,
    VscDebugStart,
    VscGripper,
    VscRemote,
    VscSync,
    VscWarning,
    VscWorkspaceUnknown,
} from "react-icons/vsc";
import { RingLoader } from "react-spinners";
import { analysisOfId, AnalysisResponse, deployBackendOfId, execOfId, getBackendStatus, undeployBackendOfId } from "../../../api/ProjectApi";
import { useAppSelector } from "../../../app/hooks";
import { BackendState, BackendStatus } from "../../../model/Project";
import { extractPlatformType } from "../../project/ProjectProfile";
import { tokenSelector } from "../../user/userSlice";
import { useBackendStatus, useDeploy, useDeployId, useEpoch, useExecInfo, useFlowPending, useHotNodes, useMetalFlow, usePlatform, useProfile } from "../DesignerProvider";
import { useAsync } from "../../../api/Hooks";
import { State } from "../../../api/State";
import { AxiosError } from "axios";
import { MetalNodeState } from "../MetalView";
import { getRecentExecOfProject } from "../../../api/ExecApi";
import { Exec, ExecState } from "../../../model/Exec";
import { GrTasks } from "react-icons/gr";
import _ from "lodash";

export interface BackendBarProps {
    id: string
}

export function BackendBar(props: BackendBarProps) {
    const {id} = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    return (
        <Paper
            square
            variant="outlined"
            style={{
                boxSizing: "border-box",
                width: "100%",
                height: "2em",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                // backgroundColor: "#FAFAFA",
            }}
        >
            <Stack direction="row" justifyContent="flex-start" alignItems="center" spacing={1}>
                <BackendControl token={token} />
                <SyncBackendStatus token={token} />
                <DeployBrief />
                <BackendStatusBrief />
                <ExecuteBar id={id} token={token}/>
            </Stack>
            <Stack direction="row" justifyContent="flex-end" alignItems="center" spacing={1}>
                <BackendNotice />
            </Stack>
        </Paper>
    );
}


interface BackendControlProps {
    token: string | null;
}

function useBackendDeploy(token: string | null, deployId?: string | null): [() => void, State] {
    const [run, status] = useAsync<void>();
    const [, setBackendStatus] = useBackendStatus();
    const [, setEpoch] = useEpoch();
    const deploy = () => {
        if (token === null) {
            return;
        }
        if (deployId !== undefined && deployId !== null) {
            run(deployBackendOfId(token, deployId).then(() => {
                return getBackendStatus(token, deployId);
            }).then(status => {
                setBackendStatus(status);
                if (status.epoch !== undefined) {
                    setEpoch(status.epoch);
                }
            }));
        }
    }

    return [deploy, status];
}

function useBackendUndeploy(token: string | null, deployId?: string | null): [() => void, State] {
    const [run, status] = useAsync<void>();
    const [, setBackendStatus] = useBackendStatus();
    const [, setEpoch] = useEpoch();
    const undeploy = () => {
        if (token === null) {
            return;
        }
        if (deployId !== undefined && deployId !== null) {
            run(undeployBackendOfId(token, deployId).then(() => {
                return getBackendStatus(token, deployId);
            }).then(status => {
                setBackendStatus(status);
                if (status.epoch !== undefined) {
                    setEpoch(status.epoch);
                }
            }));
        }
    }

    return [undeploy, status];
}


function BackendControl(props: BackendControlProps) {
    const { token } = props;
    const [backendStatus] = useBackendStatus();
    const [deployId] = useDeployId();
    const [platform] = usePlatform();
    const isCanUnDeploy = backendStatus?.current === BackendState.CREATED || backendStatus?.current === BackendState.UP;
    const platformType = extractPlatformType(platform);

    const [deploy, deployStatus] = useBackendDeploy(token, deployId);
    const [undeploy, undeployStatus] = useBackendUndeploy(token, deployId);

    const isPending = deployStatus === State.pending || undeployStatus === State.pending;

    const deployTip = () => {
        switch (deployStatus) {
            case State.failure:
                return "Fail to deploy";
            case State.pending:
                return (
                    <RingLoader size="1em" loading={true}/>
                )
            default:
                return platformType;
        }
    }

    const undeployTip = () => {
        switch (undeployStatus) {
            case State.failure:
                return "Fail to undeploy";
            case State.pending:
                return (
                    <RingLoader size="1em" loading={true}/>
                )
            default:
                return platformType;
        }
    }
    
    return (
        <Button
            size="small"
            variant="contained"
            disableElevation={true}
            disabled={isPending}
            startIcon={ isCanUnDeploy? <VscDebugDisconnect color="white"/>:<VscRemote color="white" />}
            sx={{
                backgroundColor: "orangered",
                borderRadius: "0px",
            }}
            onClick={isCanUnDeploy? undeploy: deploy}
        >
            {isCanUnDeploy? undeployTip(): deployTip()}
        </Button>
    );
}

export function DeployBrief() {
    const [deployId] = useDeployId();
    const [epoch] = useEpoch();
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);
    const onClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchor(event.currentTarget);
    };

    const onClose = () => {
        setAnchor(null);
    };

    return (
        <>
            <IconButton
                onClick={onClick}
                size="small"
                sx={{
                    borderRadius: "0px",
                }}
            >
                <VscBookmark />
            </IconButton>
            <Popover
                open={anchor !== null}
                onClose={onClose}
                anchorEl={anchor}
                anchorOrigin={{
                    vertical: "top",
                    horizontal: "left",
                }}
                transformOrigin={{
                    vertical: "bottom",
                    horizontal: "left",
                }}
                PaperProps={{
                    square: true,
                    variant: "outlined",
                    sx: {
                        boxSizing: "border-box",
                        padding: "0.5em",
                    },
                }}
            >
                <Grid container>
                    <Grid
                        item
                        xs={3}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            justifyContent: "flex-start",
                            alignItems: "flex-start",
                        }}
                    >
                        Deploy Id
                    </Grid>
                    <Grid
                        item
                        xs={9}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            justifyContent: "flex-start",
                            alignItems: "flex-start",
                        }}
                    >
                        <Typography variant="body1" color={"text.secondary"}>
                            {deployId === undefined ? "?" : deployId}
                        </Typography>
                    </Grid>
                    <Grid
                        item
                        xs={3}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            justifyContent: "flex-start",
                            alignItems: "flex-start",
                        }}
                    >
                        Epoch
                    </Grid>
                    <Grid
                        item
                        xs={9}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            justifyContent: "flex-start",
                            alignItems: "flex-start",
                        }}
                    >
                        <Typography variant="body1" color={"text.secondary"}>
                            {epoch === undefined ? "?" : epoch}
                        </Typography>
                    </Grid>
                </Grid>
            </Popover>
        </>
    );
}

function useSyncBackendStatus(token: string | null): [boolean, () => void] {
    const [deployId] = useDeployId();
    const [, setEpoch] = useEpoch();
    const [backendStatus, setBackendStatus] = useBackendStatus();
    const [isPending, startTransition] = useTransition();

    const sync = useCallback(() => {
        if (token !== null && deployId !== undefined) {
            startTransition(() => {
                getBackendStatus(token, deployId).then((status: BackendStatus) => {
                    setBackendStatus(status);
                    if (status.epoch !== undefined) {
                        setEpoch(status.epoch);
                    }
                    return status;
                });
            });
        }
    }, [deployId, setBackendStatus, setEpoch, token]);

    useEffect(() => {
        if (token === null || deployId === undefined) {
            return;
        }
        if (
            backendStatus?.current === BackendState.CREATED ||
            backendStatus?.current === BackendState.UP
        ) {
            const timer = setTimeout(() => {
                sync();
            }, 5000);
            return () => {
                clearTimeout(timer);
            };
        }
    }, [backendStatus, deployId, sync, token]);

    return [isPending, sync];
}

interface SyncBackendStatusProps {
    token: string | null;
}

function SyncBackendStatus(props: SyncBackendStatusProps) {
    const { token } = props;
    const [isPending, sync] = useSyncBackendStatus(token);
    const onSync = () => {
        sync();
    };
    return (
        <Stack direction="row" justifyContent="flex-start" alignItems="center" spacing={1}>
            {!isPending && (
                <IconButton
                    sx={{
                        borderRadius: "0px",
                    }}
                    size="small"
                    onClick={onSync}
                >
                    <VscSync />
                </IconButton>
            )}

            <RingLoader size="1em" loading={isPending} />
            {/* {isPending && (
                <Typography variant="body1" color={"text.secondary"}>
                    Syncing Backend status...
                </Typography>
            )} */}
        </Stack>
    );
}

function statusIcon(status: BackendState, color?: string) {
    switch (status) {
        case BackendState.CREATED:
            return <AiOutlineApi color={color} />;
        case BackendState.DOWN:
            return <HiStop color={color} />;
        case BackendState.UP:
            return <AiFillThunderbolt color={color} />;
        case BackendState.FAILURE:
            return <AiOutlineWarning color={color} />;
    }
}

function statusInfo(status: BackendStatus) {
    switch (status.current) {
        case BackendState.CREATED:
            return `Create at ${moment(status.createdTime).format("YYYY-MM-DD HH:mm:ss")}`;
        case BackendState.DOWN:
            return `Down at ${moment(status.downTime).format("YYYY-MM-DD HH:mm:ss")}`;
        case BackendState.UP:
            return `Up at ${moment(status.upTime).format("YYYY-MM-DD HH:mm:ss")}`;
        case BackendState.FAILURE:
            return `Fail at ${moment(status.failureTime).format("YYYY-MM-DD HH:mm:ss")}`;
    }
}

function BackendStatusBrief() {
    const [status] = useBackendStatus();
    const icon =
        status === undefined ? (
            <VscWorkspaceUnknown color={"grey"} />
        ) : (
            statusIcon(status?.current, "grey")
        );

    const tip = status === undefined ? "?" : statusInfo(status);

    return (
        <Button
            size="small"
            startIcon={icon}
            sx={{
                borderRadius: "0px",
            }}
        >
            {tip}
        </Button>
    );
}

function BackendNotice() {
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);
    const [status] = useBackendStatus();
    const isFailure = status?.current === BackendState.FAILURE;
    const icon = isFailure ? <VscWarning /> : <VscComment />;

    const onClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchor(event.currentTarget);
    };

    const onClose = () => {
        setAnchor(null);
    };

    const msg = isFailure ? (
        <Alert severity="error" variant="outlined">
            {status.failureMsg}
        </Alert>
    ) : (
        <Alert severity="info" variant="outlined"></Alert>
    );

    return (
        <>
            <IconButton
                size="small"
                onClick={onClick}
                sx={{
                    borderRadius: "0px",
                }}
            >
                {icon}
            </IconButton>
            <Popover
                open={anchor !== null}
                onClose={onClose}
                anchorEl={anchor}
                anchorOrigin={{
                    vertical: "top",
                    horizontal: "right",
                }}
                transformOrigin={{
                    vertical: "bottom",
                    horizontal: "right",
                }}
                PaperProps={{
                    square: true,
                    variant: "outlined",
                    sx: {
                        boxSizing: "border-box",
                        padding: "0.5em",
                        minWidth: "10vw",
                    },
                }}
            >
                {msg}
            </Popover>
        </>
    );
}

function useAnalysis(token: string | null, id: string): [()=>void, State, AnalysisResponse | null] {
    const [flowAction] = useMetalFlow();
    const [, setFlowPending] = useFlowPending();
    const [, setHotNodes] = useHotNodes();
    const [run, status, result] = useAsync<AnalysisResponse>({
        onSuccess: (result) => {
            const analysed = result.analysed.map(ide => {
                const r: [string, MetalNodeState] = [ide, MetalNodeState.ANALYSISED];
                return r;
            });
            const unAnalysed = result.unAnalysed.map(ide => {
                const r: [string, MetalNodeState] = [ide, MetalNodeState.UNANALYSIS];
                return r;
            });
           setFlowPending(false);
            setHotNodes([
                ...analysed,
                ...unAnalysed,
            ])
        },
        onPending: () => {
            setFlowPending(true);
            setHotNodes(
                flowAction.allNodes().map(nd => [nd.id, MetalNodeState.PENDING])
            );
        },
        onError: () => {
            setFlowPending(false);
            setHotNodes(
                flowAction.allNodes().map(nd => [nd.id, MetalNodeState.ERROR])
            );
        }
    });

    const analysis = () => {
        if (token === null) {
            return;
        }
        const spec = flowAction.export();
        run(analysisOfId(token, id, spec));
    }

    return [analysis, status, result];
}

function useExec(token: string | null, id: string): [()=>void, State] {
    const [flowAction] = useMetalFlow();
    const [, setFlowPending] = useFlowPending();
    const [, setHotNodes] = useHotNodes();
    const [,sync] = useSyncExecInfo(token, id);
    const [, setExec] = useExecInfo();
    const [run, status] = useAsync<void>({
        onPending: () => {
            setExec(undefined);
            setFlowPending(true);
            setHotNodes(
                flowAction.allNodes().map(nd => [nd.id, MetalNodeState.PENDING])
            );
        },
        onError: () => {
            setFlowPending(false);
            setHotNodes(
                flowAction.allNodes().map(nd => [nd.id, MetalNodeState.ERROR])
            );
        },
        onSuccess: () => {
            sync();
        }
    });

    const exec = () => {
        if (token === null) {
            return;
        }
        run(execOfId(token, id).then(resp => {
            if (resp.status !== "OK") {
                throw new AxiosError("Fail to execute.");
            }
        }));
    }

    return [exec, status]
}


function useSyncExecInfo(token: string | null, id: string): [boolean, ()=>void]{
    const [, setExec] = useExecInfo();
    const [deploy] = useDeploy();
    const [profile] = useProfile();
    const [flowAction] = useMetalFlow();
    const [, setFlowPending] = useFlowPending();
    const [, setHotNodes] = useHotNodes();
    const deployId = deploy.deployId;
    const epoch = deploy.epoch;

    const [run, status] = useAsync<Exec | undefined>({
        onSuccess: (recent) => {
            if (recent === undefined || deploy === undefined || profile === undefined) {
                setExec(undefined);
                return;
            } 
            const isChecked =
                deploy.deployId === recent.deploy.id &&
                deploy.epoch === recent.deploy.epoch &&
                _.isEqual(profile.pkgs.sort(), recent.deploy.pkgs.sort()) &&
                _.isEqual(profile.platform, recent.deploy.platform) &&
                _.isEqual(profile.backendArgs.sort(), recent.deploy.backend.args.sort()) &&
                _.isEqual(flowAction.export(), recent.SPEC);

            if (!isChecked) {
                setExec(undefined);
                setFlowPending(false);
                return;
            }

            setExec(recent);
            if (recent?.status === ExecState.FINISH) {
                setFlowPending(false);
                setHotNodes(flowAction.allNodes().map(nd => {
                    const rt: [string, MetalNodeState] = [nd.id, MetalNodeState.EXECED];
                    return rt;
                }));
            }
            if (recent?.status === ExecState.FAILURE) {
                setFlowPending(false);
                setHotNodes(flowAction.allNodes().map(nd => {
                    const rt: [string, MetalNodeState] = [nd.id, MetalNodeState.ERROR];
                    return rt;
                }));
            }
        },
        onError: () => {
            setHotNodes(flowAction.allNodes().map(nd => {
                const rt: [string, MetalNodeState] = [nd.id, MetalNodeState.ERROR];
                return rt;
            }));
        },
    });

    const sync = useCallback(() => {
        if (token === null) {
            return;
        }
        run(getRecentExecOfProject(token, id, deployId, epoch));
        
    }, [deployId, epoch, id, run, token]);

    return [status === State.pending, sync];
}

function isExecing(exec: Exec | undefined) {
    return exec?.status === ExecState.CREATE || exec?.status === ExecState.SUBMIT || exec?.status === ExecState.RUNNING;
}

interface SyncExecInfoProps {
    token: string | null;
    id: string;
}

function SyncExecInfo(props: SyncExecInfoProps) {
    const { token, id } = props;
    const [isPending, sync] = useSyncExecInfo(token, id);
    const [exec] = useExecInfo();
    const [,, onBackendStatusChange] = useBackendStatus();
    const onSync = () => {
        sync();
    };

    useEffect(() => {
        if (isExecing(exec)) {
            const timer = setTimeout(
                () => {
                    sync();
                },
                5000
            );
            return () => {
                clearTimeout(timer);
            }
        }

        return onBackendStatusChange((status, prev) => {
            if (status === undefined) {
                return;
            }
            if (status.current === BackendState.UP && status.current !== prev?.current) {
                sync();
            }
        });
    }, [exec, onBackendStatusChange, sync]);

    return (
        <Stack direction="row" justifyContent="flex-start" alignItems="center" spacing={1}>
            {!isPending && (
                <IconButton
                    sx={{
                        borderRadius: "0px",
                    }}
                    size="small"
                    onClick={onSync}
                >
                    <VscSync />
                </IconButton>
            )}

            <RingLoader size="1em" loading={isPending} />
            {isPending && (
                <Typography variant="body1" color={"text.secondary"}>
                    Syncing Execution status...
                </Typography>
            )}
            {!isPending && exec && (
                <Button
                    size="small"
                    variant="contained"
                    disableElevation={true}
                    disabled={isPending}
                    startIcon={<GrTasks color="white" />}
                    sx={{
                        backgroundColor: "orangered",
                        borderRadius: "0px",
                    }}
                >
                    {exec.status}
                </Button>
            )}
        </Stack>
    );
}

interface ExecuteBarProps {
    id: string,
    token: string | null,
}

function ExecuteBar(props: ExecuteBarProps) {
    const {id, token} = props;
    const [backendStatus] = useBackendStatus();
    const [mode, setMode] = useState<"ANALYSIS" | "EXEC">("ANALYSIS");
    const [analysis, analysisStatus, analysisResp] = useAnalysis(token, id)
    const [exec, execStatus] = useExec(token, id);
    const [recent] = useExecInfo();
    const isBackendUp = backendStatus?.current === BackendState.UP;
    const isAnalysising = analysisStatus === State.pending;
    const isSubmittingExec = execStatus === State.pending;
    const isExecPending = isExecing(recent);
    const isPending = isAnalysising || isSubmittingExec || isExecPending;
    const isDebugEnable = isBackendUp && !isPending;
    const isExecEnable = isBackendUp && analysisStatus === State.success && !isPending;

    const analysisTip = () => {
        switch (analysisStatus) {
            case State.failure:
                return "Fail to analysis spec."
            case State.pending:
                return "Analysising spec..."
            case State.success:
                return "Success to analysis spec."
            default:
                return "";
        }
    }

    const execTip = () => {
        switch (execStatus) {
            case State.failure:
                return "Fail to exec spec.";
            case State.pending:
                return "Submitting spec...";
            case State.success:
                return "Success to submit spec execution.";
            default:
                return "";
        }
    }

    const onAnalysis = () => {
        setMode("ANALYSIS");
        analysis();
    }

    const onExec = () => {
        setMode("EXEC");
        exec();
    }

    const syncExecInfo = useMemo(()=>(<SyncExecInfo token={token} id={id} />), [id, token]);

    return (
        <Stack
            direction="row"
            justifyContent="flex-start"
            alignItems="center"
            spacing={1}
            sx={{
                backgroundColor: "cyan",
            }}
        >            
            <Button
                size="small"
                startIcon={<VscGripper />}
                sx={{
                    borderRadius: "0px",
                }}
            >
                {isBackendUp? (mode === "EXEC"? execTip(): analysisTip()): ""}
            </Button>
            <RingLoader size="1em" loading={isPending} />
            <IconButton
                sx={{
                    borderRadius: "0px",
                }}
                size="small"
                disabled={!isDebugEnable}
                onClick={onAnalysis}
            >
                <VscDebugAltSmall />
            </IconButton>
            <IconButton
                sx={{
                    borderRadius: "0px",
                }}
                size="small"
                disabled={!isExecEnable}
                onClick={onExec}
            >
                <VscDebugStart />
            </IconButton>
            {isBackendUp && syncExecInfo}
        </Stack>
    );
}
