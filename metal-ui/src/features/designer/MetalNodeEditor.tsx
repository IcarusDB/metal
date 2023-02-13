import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Alert,
    Button,
    Container,
    Grid,
    IconButton,
    LinearProgress,
    List,
    ListItem,
    Paper,
    Skeleton,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import { RJSFSchema } from "@rjsf/utils";
import { IChangeEvent } from "@rjsf/core";
import { Form } from "@rjsf/mui";
import validator from "@rjsf/validator-ajv8";
import { Metal, MetalTypes } from "../../model/Metal";
import { MetalNodeProps, MetalNodeState } from "./MetalView";
import { VscArrowLeft, VscExpandAll, VscRefresh, VscTable } from "react-icons/vsc";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { useDeployId, useHotNodes, useMetalFlow, useMetalNodeEditor } from "./DesignerProvider";
import { IReadOnly } from "../ui/Commons";
import _ from "lodash";
import { useAsync } from "../../api/Hooks";
import { schemaOfId, SchemaResponse } from "../../api/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { State } from "../../api/State";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { vscDarkPlus } from "react-syntax-highlighter/dist/esm/styles/prism";
import { ClockLoader, SyncLoader } from "react-spinners";

function useSchema(
    token: string | null,
    deployId: string | null,
    metalId: string
): [() => void, State, SchemaResponse | null] {
    const [run, status, result] = useAsync<SchemaResponse>();
    const schema = useCallback(() => {
        if (token === null || deployId === null) {
            return;
        }
        run(schemaOfId(token, deployId, metalId));
    }, [deployId, metalId, run, token]);

    return [schema, status, result];
}

export interface MetalNodeSchemaProps {
    id: string;
    name?: string;
}

export function MetalNodeSchema(props: MetalNodeSchemaProps) {
    const { id, name } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [deployId] = useDeployId();
    const [schema, schemaStatus, result] = useSchema(
        token,
        deployId === undefined ? null : deployId,
        id
    );

    const isPending = schemaStatus === State.pending;
    const progress = isPending ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );
    useEffect(() => {
        schema();
    }, [schema]);

    return (
        <Accordion
            sx={{
                width: "100%",
            }}
        >
            <AccordionSummary id={`${id}-header`} expandIcon={<VscExpandAll />}>
                <div
                    style={{
                        display: "flex",
                        flexDirection: "row",
                        flexWrap: "wrap",
                        alignContent: "center",
                        justifyContent: "flex-start",
                        alignItems: "center",
                        width: "100%",
                    }}
                >
                    <IconButton>
                        <VscTable />
                    </IconButton>
                    <Typography>{name === undefined ? id : name}</Typography>
                </div>
            </AccordionSummary>
            <AccordionDetails>
                <div
                    style={{
                        position: "relative",
                        width: "100%",
                        height: "100%",
                        overflow: "hidden",
                    }}
                >
                    <Button
                        sx={{ width: "100%" }}
                        onClick={schema}
                        variant={"outlined"}
                        startIcon={<VscRefresh />}
                        disabled={isPending}
                    >
                        {isPending ? "Syncing" : "Sync"}
                    </Button>
                    {progress}
                    <div
                        style={{
                            position: "relative",
                            maxHeight: "30vh",
                            width: "100%",
                            overflow: "auto",
                        }}
                    >
                        <SyntaxHighlighter language={"json"} style={vscDarkPlus}>
                            {JSON.stringify(result, null, 2)}
                        </SyntaxHighlighter>
                        <ResizeBackdrop open={isPending} />
                    </div>
                </div>
            </AccordionDetails>
        </Accordion>
    );
}

export interface MetalNodeEditorProps extends IReadOnly {}

export const MetalNodeEditor = (props: MetalNodeEditorProps) => {
    const { isReadOnly } = props;
    const [metalFlowAction] = useMetalFlow();
    const [metalProps, setMetalProps] = useState<MetalNodeProps | null>(null);
    const [isOpen, setOpen] = useState(false);
    const nameInputRef = useRef<HTMLInputElement>();
    const [, setNodeEditorAction] = useMetalNodeEditor();
    const [, setHotNodes] = useHotNodes();
    const id = metalProps?.metal.id;
    const inputs = metalProps === null ? (id: string) => [] : metalProps.inputs;

    const readOnly = () => {
        return isReadOnly || metalProps?.status === MetalNodeState.PENDING;
    };
    const printInputs = () => {
        const inputs = metalFlowAction.inputs;
        if (metalProps === null) {
            return;
        }
        console.log(inputs(metalProps.metal.id));
    };

    const load = useCallback((props: MetalNodeProps) => {
        setMetalProps(props);
        setOpen(true);
    }, []);

    const close = useCallback(() => {
        setMetalProps(null);
        setOpen(false);
    }, []);

    useMemo(() => {
        setNodeEditorAction({
            load: load,
            close: close,
        });
    }, [close, setNodeEditorAction, load]);

    if (metalProps == null) {
        return <Skeleton></Skeleton>;
    }
    const metal = metalProps.metal;
    const schema = metalProps.metalPkg.formSchema;
    const uiSchema = metalProps.metalPkg.uiSchema;

    const onConfirm = (data: IChangeEvent<any, RJSFSchema, any>) => {
        let newName = metal.name;
        if (nameInputRef !== null && nameInputRef.current !== undefined) {
            newName = nameInputRef.current.value;
        }
        const newMetal: Metal = {
            ...metal,
            name: newName,
            props: data.formData,
        };
        metalProps.onUpdate(newMetal);
        setMetalProps(null);
        setOpen(false);
    };

    const onCancel = () => {
        setMetalProps(null);
        setOpen(false);
    };

    const hasNoInputs = () =>
        metalProps.type === MetalTypes.SOURCE || metalProps.type === MetalTypes.SETUP;
    const isAnalysised = (nodeProps: MetalNodeProps) => {
        const metalStatus: MetalNodeState =
            nodeProps.status === undefined ? MetalNodeState.UNANALYSIS : nodeProps.status;
        return metalStatus === MetalNodeState.ANALYSISED;
    };

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
                    <IconButton onClick={onCancel}>
                        <VscArrowLeft />
                    </IconButton>
                </Paper>
                <Grid
                    container
                    spacing={1}
                    sx={{
                        boxSizing: "border-box",
                        margin: "0px",
                        position: "absolute",
                        left: "0px",
                        right: "0px",
                        top: "5.5vh",
                        bottom: "1vh",
                    }}
                >
                    <Grid item xs={3}>
                        <Paper
                            square
                            variant="outlined"
                            sx={{
                                boxSizing: "border-box",
                                padding: "1em",
                            }}
                        >
                            <Typography>Inputs Schemas</Typography>
                        </Paper>
                        <Paper
                            square
                            sx={{
                                boxSizing: "border-box",
                                height: "90%",
                                width: "100%",
                                padding: "1em",
                            }}
                        >
                            {isReadOnly && (
                                <Alert variant="outlined" severity="info">
                                    {`Now is In readonly mode.`}
                                </Alert>
                            )}
                            {!isReadOnly && hasNoInputs() && (
                                <Alert variant="outlined" severity="info">
                                    {`Inputs Schemas [Node is ${metalProps.type} and hasn't any inputs.]`}
                                </Alert>
                            )}
                            {!isReadOnly && !hasNoInputs() && id && (
                                <div
                                    style={{
                                        position: "relative",
                                        width: "100%",
                                        height: "100%",
                                        overflow: "hidden",
                                    }}
                                >
                                    <List
                                        sx={{
                                            position: "relative",
                                            width: "100%",
                                            maxHeight: "50vh",
                                            overflow: "auto",
                                        }}
                                    >
                                        {inputs(id)
                                            .filter((nd) => isAnalysised(nd.data))
                                            .map((nd) => (
                                                <ListItem key={nd.id}>
                                                    <MetalNodeSchema
                                                    id={nd.id}
                                                    name={nd.data.metal.name}
                                                    />
                                                </ListItem>
                                                
                                            ))}
                                        {inputs(id)
                                            .filter((nd) => !isAnalysised(nd.data))
                                            .map((nd) => (
                                                <ListItem key={nd.id}>
                                                    <Alert severity="info" variant="outlined">
                                                        {`${nd.data.metal.name}[${MetalNodeState.UNANALYSIS}]`}
                                                    </Alert>
                                                </ListItem>
                                                
                                            ))}
                                    </List>
                                </div>
                            )}
                        </Paper>
                    </Grid>
                    <Grid item xs={9}>
                        <Paper
                            sx={{
                                position: "absolute",
                                height: "-webkit-fill-available",
                                width: "-webkit-fill-available",
                                overflowY: "hidden",
                            }}
                        >
                            <Container
                                sx={{
                                    margin: "0px",
                                    height: "100%",
                                    overflowY: "auto",
                                    display: "block",
                                }}
                            >
                                <Stack
                                    direction="column"
                                    justifyContent="flex-start"
                                    alignItems="flex-start"
                                    spacing={2}
                                >
                                    <div></div>
                                    <TextField
                                        id={"name"}
                                        label={"name"}
                                        defaultValue={metal.name}
                                        inputRef={nameInputRef}
                                        disabled={readOnly()}
                                    />
                                    <Form
                                        schema={schema}
                                        uiSchema={uiSchema}
                                        validator={validator}
                                        formData={metalProps.metal.props}
                                        onSubmit={onConfirm}
                                        readonly={readOnly()}
                                    >
                                        <Stack
                                            direction="row"
                                            justifyContent="flex-start"
                                            alignItems="center"
                                            spacing={8}
                                        >
                                            <Button
                                                type={"submit"}
                                                variant={"contained"}
                                                disabled={readOnly()}
                                            >
                                                {"confirm"}
                                            </Button>
                                        </Stack>
                                    </Form>
                                    <div></div>
                                </Stack>
                            </Container>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        </ResizeBackdrop>
    );
};
