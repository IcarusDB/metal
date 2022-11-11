import {FormEvent, ForwardedRef, forwardRef, useImperativeHandle, useRef, useState} from "react";
import {MetalNodeProps} from "./MetalView";
import {Container, Drawer, Skeleton} from "@mui/material";
import {RJSFSchema} from "@rjsf/utils";
import {IChangeEvent} from "@rjsf/core";
import {Form} from "@rjsf/mui";
import validator from "@rjsf/validator-ajv8";
import {Metal} from "../../model/Metal";

export interface MetalNodeEditorProps {
}

export interface MetalNodeEditorRef {
    load: (props: MetalNodeProps) => void;
    close: () => void
}

export const MetalNodeEditor = forwardRef((props: MetalNodeEditorProps, ref: ForwardedRef<MetalNodeEditorRef>) => {
        const [metalProps, setMetalProps] = useState<MetalNodeProps | null>(null)
        const [isOpen, setOpen] = useState(false)

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

        const schema = metalProps.metalPkg.formSchema
        const uiSchema = metalProps.metalPkg.uiSchema

        const onConfirm = (data: IChangeEvent<Metal, RJSFSchema, any>, event: FormEvent<any>) => {
            console.log(data)
        }

        return (
            <Drawer
                anchor={"right"}
                open={isOpen}
                sx={{height: '100%'}}
            >
                <Container>
                    <Form
                        schema={schema}
                        uiSchema={uiSchema}
                        validator={validator}
                        formData={metalProps.metal}
                        onSubmit={onConfirm}
                    >
                    </Form>
                </Container>
            </Drawer>
        )
    }
)

