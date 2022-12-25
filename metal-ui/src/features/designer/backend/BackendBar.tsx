import { Alert, Button, Grid, IconButton, Paper, Popover, Stack, Typography } from "@mui/material";
import moment from "moment";
import { useState } from "react";
import { AiFillThunderbolt, AiOutlineApi, AiOutlineWarning } from "react-icons/ai";
import { HiStop } from "react-icons/hi";
import {
    VscBookmark,
    VscComment,
    VscFlame,
    VscRemote,
    VscWarning,
    VscWorkspaceUnknown,
} from "react-icons/vsc";
import { BackendState, BackendStatus } from "../../../model/Project";
import { useBackendStatus, useDeploy } from "../DesignerProvider";

export interface BackendBarProps {}

export function BackendBar() {
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
                <Button
                    size="small"
                    variant="contained"
                    disableElevation={true}
                    startIcon={<VscRemote color="white" />}
                    sx={{
                        backgroundColor: "orangered",
                        borderRadius: "0px",
                    }}
                ></Button>
                <DeployBrief />
                <BackendStatusBrief />
            </Stack>
            <Stack direction="row" justifyContent="flex-end" alignItems="center" spacing={1}>
                <BackendNotice />
            </Stack>
        </Paper>
    );
}

export function DeployBrief() {
    const [{ deployId, epoch }] = useDeploy();
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
