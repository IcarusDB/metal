import {ImUpload, ImDownload} from "react-icons/im"
import {AiOutlineFunction, AiOutlineDelete} from "react-icons/ai"
import {VscMerge, VscExpandAll} from "react-icons/vsc"
import {NodeProps} from "reactflow";
import {Handle, Position} from 'reactflow';
import {
    Avatar,
    Box,
    Grid,
    Button,
    IconButton,
    Paper,
    Stack,
    Container,
    Badge,
    Divider,
    ButtonGroup,
    Typography
} from "@mui/material";
import {useMemo} from "react";
import {MetalPkg} from "../../model/MetalPkg";
import {Metal, MetalTypes} from "../../model/Metal";

export const MetalViewIcons = {
    SOURCE: <ImUpload/>,
    SINK: <ImDownload/>,
    MAPPER: <AiOutlineFunction/>,
    FUSION: <VscMerge/>
}

export interface MetalNodeProps {
    metalPkg: MetalPkg,
    metal: Metal,
    type: MetalTypes
}

export interface IMetalNodeView {
    inputHandle: (props: MetalNodeProps) => JSX.Element
    outputHandle: (props: MetalNodeProps) => JSX.Element
    logo: (props: MetalNodeProps) => JSX.Element
}

export const MetalSourceNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return (
            <></>
        )
    },
    outputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"source"} id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
        )
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SOURCE
    }
}

export const MetalSinkNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"target"} id={`${props.metal.id}-input`} position={Position.Top}></Handle>
        )
    },
    outputHandle: (props: MetalNodeProps) => {
        return (
            <></>
        )
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SINK
    }
}

export const MetalMapperNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"target"} id={`${props.metal.id}-input`} position={Position.Top}></Handle>
        )
    },
    outputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"source"} id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
        )
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.MAPPER
    }
}

export const MetalFusionNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"target"}  id={`${props.metal.id}-input`} position={Position.Top}></Handle>
        )
    },
    outputHandle: (props: MetalNodeProps) => {
        return (
            <Handle type={"source"}  id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
        )
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.FUSION
    }
}

export const MetalNodeViews = {
    metalNodeView: (type: string) => {
        switch (type) {
            case MetalTypes.SOURCE: {
                return MetalSourceNodeView
            }
                ;
            case MetalTypes.SINK: {
                return MetalSinkNodeView
            }
                ;
            case MetalTypes.FUSION: {
                return MetalFusionNodeView
            }
                ;
            case MetalTypes.MAPPER: {
                return MetalMapperNodeView
            }
                ;
            default: {
                return MetalSourceNodeView
            }
        }
    }
}


export function MetalNode(props: NodeProps<MetalNodeProps>) {
    const {metal, metalPkg, type} = props.data
    const nodeView: IMetalNodeView = MetalNodeViews.metalNodeView(type)

    return (
        <Badge color="secondary" badgeContent={"?"}>
            <Box sx={{flexGrow: 0, paddingLeft: "1em", paddingRight: "0em"}} component={Paper}>
                {nodeView.inputHandle(props.data)}
                <Stack
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    spacing={1}
                >
                    {nodeView.logo(props.data)}
                    <Divider orientation="vertical" flexItem/>
                    <Stack
                        direction="column"
                        justifyContent="space-around"
                        alignItems="flex-start"
                    >
                        <Typography variant={"h6"}>{metal.name}</Typography>
                        <Typography variant={"caption"} color={"GrayText"}>{metalPkg.class}</Typography>
                    </Stack>
                    <Stack
                        direction="column"
                        justifyContent="flex-end"
                        alignItems="stretch"
                    >
                        <Button size="small" sx={{borderTopLeftRadius: 0}}><VscExpandAll/></Button>
                        <Button size="small" sx={{borderBottomLeftRadius: 0}}><AiOutlineDelete/></Button>
                    </Stack>
                </Stack>
                {nodeView.outputHandle(props.data)}
            </Box>
        </Badge>


    )
}

export const MetalNodeTypes = {
    metal: MetalNode
}