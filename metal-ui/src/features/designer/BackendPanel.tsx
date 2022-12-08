import { Chip, Divider, Grid, IconButton, Paper, Skeleton, Typography } from "@mui/material";
import moment from "moment";
import {
    ForwardedRef,
    forwardRef,
    useCallback,
    useEffect,
    useImperativeHandle,
    useState,
} from "react";
import { VscDebugCoverage, VscDebugStart, VscVmConnect, VscVmOutline } from "react-icons/vsc";
import { useAsync } from "../../api/Hooks";
import { getDeploy } from "../../api/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { BackendState, BackendStatus, Deploy } from "../../model/Project";
import { tokenSelector } from "../user/userSlice";

export function backendStatusColor(status: BackendState) {
    switch (status) {
        case BackendState.UN_DEPLOY:
            return "primary";
        case BackendState.UP:
            return "success";
        case BackendState.DOWN:
            return "info";
        case BackendState.FAILURE:
            return "error";
    }
}
export interface BackendStatusViewProps {
    status?: BackendStatus;
}

export function BackendStatusView(props: BackendStatusViewProps) {
    const { status } = props;
    if (status === undefined) {
        return <Chip label="No Backend" variant="outlined" color="default" />;
    }
    const color = backendStatusColor(status.current);
    return <Chip label={status.current} variant="filled" color={color} />;
}

export interface BackendPanelProps {
    deployId: string;
}

export interface BackendPanelHandler {
    load: () => void;
}

export const BackendPanel = forwardRef(
    (props: BackendPanelProps, ref: ForwardedRef<BackendPanelHandler>) => {
        const token: string | null = useAppSelector((state) => {
            return tokenSelector(state);
        });
        const { deployId } = props;
        const [fetchDeploy, deployStatus, deploy, fetchDeployError] = useAsync<Deploy>();

        const onLoad = useCallback(() => {
            if (token === null) {
                return;
            }
            fetchDeploy(getDeploy(token, deployId));
        }, [deployId, fetchDeploy, token]);

        useImperativeHandle(
            ref,
            () => ({
                load: onLoad,
            }),
            [onLoad]
        );

        useEffect(() => {
            onLoad();
        }, [onLoad]);

        if (deploy === null) {
            return <Skeleton />;
        }
        return (
            <Paper
                square
                variant="outlined"
                sx={{
                    minWidth: "15em",
                    maxWidth: "20em",
                    minHeight: "20em",
                }}
            >
                <Grid container spacing={1}>
                    <Grid
                        item
                        xs={6}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <IconButton>
                            <VscVmConnect />
                        </IconButton>
                        <IconButton>
                            <VscVmOutline />
                        </IconButton>
                    </Grid>
                    <Grid
                        item
                        xs={6}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Divider variant="middle" orientation="vertical" flexItem />
                        <IconButton>
                            <VscDebugCoverage />
                        </IconButton>
                        <IconButton>
                            <VscDebugStart />
                        </IconButton>
                    </Grid>
                    <Grid item xs={12}>
                        <Divider />
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "flex-start",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Deploy ID"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "flex-start",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Typography>{deploy.id}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Epoch"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Typography>{deploy.epoch}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {deploy.backend.status !== undefined && <Typography>{"Status"}</Typography>}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {deploy.backend.status !== undefined && (
                            <BackendStatusView status={deploy.backend.status} />
                        )}
                    </Grid>

                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {deploy.backend.status !== undefined && (
                            <Typography>{"Message"}</Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {deploy.backend.status !== undefined &&
                            deploy.backend.status.current === BackendState.FAILURE && (
                                <Typography>{deploy.backend.status.failureMsg}</Typography>
                            )}
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {deploy.backend.status?.upTime !== undefined && (
                            <Typography>{"Up Time"}</Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {deploy.backend.status?.upTime !== undefined && (
                            <Typography>
                                {moment(deploy.backend.status.upTime).format("YYYY-MM-DD HH:mm:ss")}
                            </Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {deploy.backend.status?.downTime !== undefined && (
                            <Typography>{"Down Time"}</Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {deploy.backend.status?.downTime !== undefined && (
                            <Typography>
                                {moment(deploy.backend.status.downTime).format(
                                    "YYYY-MM-DD HH:mm:ss"
                                )}
                            </Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {deploy.backend.status?.failureTime !== undefined &&
                            deploy.backend.status.current === BackendState.FAILURE && (
                                <Typography>{"Failure Time"}</Typography>
                            )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {deploy.backend.status?.failureTime !== undefined &&
                            deploy.backend.status.current === BackendState.FAILURE && (
                                <Typography>
                                    {moment(deploy.backend.status.failureTime).format(
                                        "YYYY-MM-DD HH:mm:ss"
                                    )}
                                </Typography>
                            )}
                    </Grid>
                </Grid>
            </Paper>
        );
    }
);
