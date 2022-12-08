import { Divider, Grid, IconButton, Paper, Skeleton, Typography } from "@mui/material";
import { ForwardedRef, forwardRef, useImperativeHandle, useState } from "react";
import { VscDebugCoverage, VscDebugStart, VscVmConnect, VscVmOutline } from "react-icons/vsc";
import { useAsync } from "../../api/Hooks";
import { Deploy } from "../../model/Project";

export interface BackendPanelProps {
    deployId: string
}

export interface BackendPanelHandler {
    load: () => void;
}

export const BackendPanel = forwardRef((props: BackendPanelProps, ref: ForwardedRef<BackendPanelHandler>) => {
    const {deployId} = props;
    const [fetchDeploy, deployStatus, deploy, fetchDeployError] = useAsync<Deploy>()

    const onLoad = () => {
    };

    useImperativeHandle(ref, ()=>({
        load: onLoad
    }), [])

    if (deploy === null) {
        return (
            <Skeleton />
        )
    }
    return (
        <Paper
            square
            variant="outlined"
            sx={{
                minWidth: "15em",
                minHeight: "20em",
            }}
        >
            <Grid container spacing={1}>
                <Grid item xs={6}
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
                <Grid item xs={6}
                    sx={{
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "flex-start",
                    }}
                >
                    <Divider variant="middle" orientation="vertical" flexItem/>
                    <IconButton>
                        <VscDebugCoverage />
                    </IconButton>
                    <IconButton>
                        <VscDebugStart />
                    </IconButton>
                </Grid>
                <Grid item xs={12}>
                    <Divider/>    
                </Grid>
                <Grid item xs={4}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-end",
                }}
                >
                    <Typography>{"Deploy ID"}</Typography>
                </Grid>
                <Grid item xs={8}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-start",
                }}
                >
                    <Typography>{deploy.id}</Typography>
                </Grid>
                <Grid item xs={4}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-end",
                }}
                >
                    <Typography>{"Epoch"}</Typography>
                </Grid>
                <Grid item xs={8}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-start",
                }}
                >
                    <Typography>{deploy.epoch}</Typography>
                </Grid>
                <Grid item xs={4}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-end",
                }}
                >
                    <Typography>{"Backend Status"}</Typography>
                </Grid>
                <Grid item xs={8}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-start",
                }}
                >
                    <Typography>{}</Typography>
                </Grid>
                <Grid item xs={4}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-end",
                }}
                >
                    <Typography>{"Message"}</Typography>
                </Grid>
                <Grid item xs={8}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-start",
                }}
                >
                    <Typography>{}</Typography>
                </Grid>
            </Grid>
        </Paper>
    )
})