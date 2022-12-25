import { Button, Grid, IconButton, Paper, Popover, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { VscBookmark, VscComment, VscRemote } from "react-icons/vsc";
import { useDeploy } from "../DesignerProvider";

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
            </Stack>
            <Stack direction="row" justifyContent="flex-end" alignItems="center" spacing={1}>
                <IconButton
                    size="small"
                    sx={{
                        borderRadius: "0px",
                    }}
                >
                    <VscComment />
                </IconButton>
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
                        padding: "0.5em" ,
                    }
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
                {/* <Paper
                    square
                    variant="outlined"
                    sx={{
                        boxSizing: "border-box",
                        margin: "0.5em",
                    }}
                >
                    
                </Paper> */}
            </Popover>
        </>
    );
}
