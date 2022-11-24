import {
    FormEvent,
    ForwardedRef,
    forwardRef,
    MouseEvent,
    RefObject,
    useImperativeHandle,
    useRef,
    useState,
} from "react";
import {
    Button,
    Container,
    Grid,
    IconButton,
    Paper,
    Skeleton,
    Stack,
    TextField,
} from "@mui/material";
import { RJSFSchema } from "@rjsf/utils";
import { IChangeEvent } from "@rjsf/core";
import { Form } from "@rjsf/mui";
import validator from "@rjsf/validator-ajv8";
import { Metal } from "../../model/Metal";
import { MetalFlowHandler } from "./MetalFlow";
import { MetalNodeProps } from "./MetalView";
import { VscArrowLeft } from "react-icons/vsc";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";

export interface MetalNodeEditorProps {
    metalFlowRef: RefObject<MetalFlowHandler>;
}

export interface MetalNodeEditorHandler {
    load: (props: MetalNodeProps) => void;
    close: () => void;
}

export const MetalNodeEditor = forwardRef(
    (props: MetalNodeEditorProps, ref: ForwardedRef<MetalNodeEditorHandler>) => {
        const metalFlowRef = props.metalFlowRef;
        const [metalProps, setMetalProps] = useState<MetalNodeProps | null>(null);
        const [isOpen, setOpen] = useState(false);
        const nameInputRef = useRef<HTMLInputElement>();

        const printInputs = () => {
            if (metalFlowRef === null || metalFlowRef.current === null) {
                return;
            }
            const inputs = metalFlowRef.current.inputs;
            if (metalProps === null) {
                return;
            }
            console.log(inputs(metalProps.metal.id));
        };

        useImperativeHandle(
            ref,
            () => ({
                load: (props: MetalNodeProps) => {
                    setMetalProps(props);
                    setOpen(true);
                },
                close: () => {
                    setMetalProps(null);
                    setOpen(false);
                },
            }),
            []
        );

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
                                sx={{
                                    height: "100%",
                                    width: "100%",
                                }}
                            ></Paper>
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
                                        />
                                        <Form
                                            schema={schema}
                                            uiSchema={uiSchema}
                                            validator={validator}
                                            formData={metalProps.metal.props}
                                            onSubmit={onConfirm}
                                        >
                                            <Stack
                                                direction="row"
                                                justifyContent="flex-start"
                                                alignItems="center"
                                                spacing={8}
                                            >
                                                <Button type={"submit"} variant={"contained"}>
                                                    {"confirm"}
                                                </Button>

                                                <Button onClick={printInputs}>{"inputs"}</Button>
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
    }
);
