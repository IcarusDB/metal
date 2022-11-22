import {FormEvent, ForwardedRef, forwardRef, MouseEvent, RefObject, useImperativeHandle, useRef, useState} from "react";
import {Backdrop, Button, Container, Paper, Skeleton, Stack, TextField} from "@mui/material";
import {RJSFSchema} from "@rjsf/utils";
import {IChangeEvent} from "@rjsf/core";
import {Form} from "@rjsf/mui";
import validator from "@rjsf/validator-ajv8";
import {Metal} from "../../model/Metal";
import { MetalFlowHandler } from "./MetalFlow";
import { MetalNodeProps } from "./MetalView";

export interface MetalNodeEditorProps {
    metalFlowRef: RefObject<MetalFlowHandler>
}

export interface MetalNodeEditorHandler {
    load: (props: MetalNodeProps) => void;
    close: () => void
}

export const MetalNodeEditor = forwardRef((props: MetalNodeEditorProps, ref: ForwardedRef<MetalNodeEditorHandler>) => {
        const metalFlowRef = props.metalFlowRef
        const [metalProps, setMetalProps] = useState<MetalNodeProps | null>(null)
        const [isOpen, setOpen] = useState(false)
        const nameInputRef = useRef<HTMLInputElement>()

        const printInputs = ()=> {
            if (metalFlowRef === null || metalFlowRef.current === null) {
                return
            }
            const inputs = metalFlowRef.current.inputs;
            if (metalProps === null) {
                return;
            }
            console.log(inputs(metalProps.metal.id))
        }

        useImperativeHandle(ref, () => ({
            load: (props: MetalNodeProps) => {
                setMetalProps(props)
                setOpen(true)
            },
            close: () => {
                setMetalProps(null)
                setOpen(false)
            }
        }), [])

        if (metalProps == null) {
            return (
                <Skeleton></Skeleton>
            )
        }
        const metal = metalProps.metal
        const schema = metalProps.metalPkg.formSchema
        const uiSchema = metalProps.metalPkg.uiSchema

        const onConfirm = (data: IChangeEvent<any, RJSFSchema, any>, event: FormEvent<any>) => {
            let newName = metal.name
            if (nameInputRef !== null && nameInputRef.current !== undefined) {
                newName = nameInputRef.current.value
            }
            const newMetal: Metal = {
                ...metal,
                name: newName,
                props: data.formData
            }
            metalProps.onUpdate(newMetal)
            setMetalProps(null)
            setOpen(false)
        }

        const onCancel = (e: MouseEvent<HTMLButtonElement> | MouseEvent<HTMLAnchorElement>) => {
            setMetalProps(null)
            setOpen(false)
        }

        return (
            <Backdrop
                open={isOpen}
                sx={{height: '100%', position: "absolute"}}
            >
                <Container component={Paper} sx={{paddingTop: "1em", paddingBottom: "1em"}}>
                    <Container component={Paper} sx={{paddingTop: "1rem", paddingBottom: "1rem"}}>
                        <Stack
                            direction="column"
                            justifyContent="center"
                            alignItems="flex-start"
                        >
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
                                    <Button type={"submit"} variant={"contained"}>{"confirm"}</Button>
                                    <Button onClick={onCancel}>{"cancel"}</Button>
                                    <Button onClick={printInputs}>{"inputs"}</Button>
                                </Stack>
                            </Form>
                        </Stack>
                    </Container>
                </Container>
            </Backdrop>
        )
    }
)

