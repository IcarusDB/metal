import {
    FormControl,
    FormControlLabel,
    FormGroup,
    FormLabel,
    Grid,
    IconButton,
    Paper,
    Switch,
    Typography,
} from "@mui/material";
import { Form } from "@rjsf/mui";
import { RJSFSchema } from "@rjsf/utils";
import { IChangeEvent } from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { ForwardedRef, forwardRef, RefObject, useImperativeHandle, useState } from "react";
import { VscArrowLeft } from "react-icons/vsc";
import { PlatformType } from "../../model/Project";
import { Container, height, Stack } from "@mui/system";

export function ProjectBasicProfile() {
    const formSchema: RJSFSchema = {
        type: "object",
        required: ["name", "platform"],
        properties: {
            name: {
                type: "string",
                title: "Project Name",
            },
            platform: {
                type: "string",
                title: "Platform Type",
                enum: Object.values(PlatformType),
            },
        },
    };

    return (
        <Paper
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Form schema={formSchema} validator={validator}></Form>
        </Paper>
    );
}

export const PkgSelector = () => {
    const [isAuto, setAuto] = useState(true);

    const onSwitch = () => {
        setAuto(!isAuto);
    };

    const mode = () => (isAuto ? "Auto mode." : "Custom mode.");

    return (
        <Paper
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Grid
                container
                spacing={0}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    height: "100%",
                    width: "100%",
                    alignContent: "flex-start",
                    justifyContent: "flex-start",
                }}
            >
                <Grid item xs={12}>
                    <Typography variant="h6">{"Metal Package Selector"}</Typography>
                </Grid>
                <Grid item xs={12}>
                    <Paper
                        square
                        variant="outlined"
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            flexWrap: "wrap",
                            alignContent: "center",
                            justifyContent: "flex-start",
                            alignItems: "center",
                        }}
                    >
                        <Switch checked={isAuto} onChange={onSwitch} />
                        <Typography variant="body1">{mode()}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12}></Grid>
            </Grid>
        </Paper>
    );
};

export interface ProjectProfileProps {
    open: boolean;
}

export interface ProjectProfileHandler {
    open: () => void;
    close: () => void;
}

export const ProjectProfile = forwardRef(
    (props: ProjectProfileProps, ref: ForwardedRef<ProjectProfileHandler>) => {
        const { open } = props;
        const [isOpen, setOpen] = useState(open);

        const close = () => {
            setOpen(false);
        };

        useImperativeHandle(
            ref,
            () => ({
                open: () => {
                    setOpen(true);
                },
                close: close,
            }),
            []
        );

        return (
            <ResizeBackdrop open={isOpen} backgroundColor={"#f4f4f4"} opacity={"1"}>
                <div
                    style={{
                        position: "absolute",
                        boxSizing: "border-box",
                        margin: "0px",
                        width: "100%",
                        height: "100%",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "flex-start",
                        justifyContent: "flex-start",
                    }}
                >
                    <Paper
                        square
                        variant="outlined"
                        sx={{
                            boxSizing: "border-box",
                            margin: "0px",
                            width: "100%",
                            height: "5vh",
                            display: "flex",
                            flexDirection: "row",
                            alignContent: "space-between",
                            justifyContent: "flex-start",
                        }}
                    >
                        <IconButton onClick={close}>
                            <VscArrowLeft />
                        </IconButton>
                    </Paper>
                    {/* <ProjectBasicProfile />  */}
                    <PkgSelector />
                </div>
            </ResizeBackdrop>
        );
    }
);
