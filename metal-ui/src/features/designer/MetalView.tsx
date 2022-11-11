import {ImUpload, ImDownload} from "react-icons/im"
import {AiOutlineFunction, AiOutlineDelete} from "react-icons/ai"
import {VscMerge, VscExpandAll} from "react-icons/vsc"
import {Connection, Node, Edge, NodeProps} from "reactflow";
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
import {RefObject, useMemo, MouseEvent} from "react";
import {MetalPkg} from "../../model/MetalPkg";
import {Metal, Metals, MetalTypes} from "../../model/Metal";
import {GraphTopology} from "../../model/GraphTopology";
import {MetalNodeEditorHandler} from "./MetalNodeEditor";

export const MetalViewIcons = {
    SOURCE: <ImUpload/>,
    SINK: <ImDownload/>,
    MAPPER: <AiOutlineFunction/>,
    FUSION: <VscMerge/>
}

export interface MetalNodeProps {
    metalPkg: MetalPkg,
    metal: Metal,
    type: MetalTypes,
    onUpdate: (newMetal: Metal) => void;
    onDelete: () => void;
    editorRef?: RefObject<MetalNodeEditorHandler>
}

export interface IMetalNodeView {
    inputHandle: (props: MetalNodeProps) => JSX.Element
    outputHandle: (props: MetalNodeProps) => JSX.Element
    logo: (props: MetalNodeProps) => JSX.Element
}

const inputHandle = (props: MetalNodeProps) => {
    return (
        <Handle type={"target"} id={`${props.metal.id}-input`} position={Position.Top}></Handle>
    )
}

const outputHandle = (props: MetalNodeProps) => {
    return (
        <Handle type={"source"} id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
    )
}

export const MetalSourceNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return (
            <></>
        )
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props)
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SOURCE
    }
}

export const MetalSinkNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props)
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
        return inputHandle(props)
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props)
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.MAPPER
    }
}

export const MetalFusionNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props)
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props)
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

export function onConnectValid(connection: Connection, nodes: Node<MetalNodeProps>[], edges: Edge[]) {
    if (connection.target === null || connection.source === null) {
        return false
    }

    if (connection.target === connection.source) {
        return false
    }

    const nodesOnGraph = new Set<string>(nodes.map(node => node.id))
    const edgesOnGraph = edges.map(edge => {
        return {source: edge.source, target: edge.target}
    })

    const graphTopology: GraphTopology = new GraphTopology(nodesOnGraph, edgesOnGraph)
    let isValid = true
    const inputs: Node<MetalNodeProps>[] = nodes.filter(node => (node.id === connection.target))
    inputs.forEach(node => {
        isValid = isValid &&
            Metals.metal(node.data.type).hasInput() &&
            Metals.metal(node.data.type).canAddInput(graphTopology, node.id)
    })
    if (!isValid) {
        return false
    }

    const outputs: Node<MetalNodeProps>[] = nodes.filter(node => (node.id === connection.source))
    outputs.forEach(node => {
        isValid = isValid && Metals.metal(node.data.type).hasOutput()
    })
    if (!isValid) {
        return false
    }

    const next = graphTopology.next(connection.target)
    return !next.has(connection.source)
}




export function MetalNode(props: NodeProps<MetalNodeProps>) {
    const {metal, metalPkg, type, onDelete, onUpdate} = props.data
    const editorRef = props.data.editorRef
    const nodeView: IMetalNodeView = MetalNodeViews.metalNodeView(type)

    const onEdit = (event: MouseEvent<HTMLAnchorElement> | MouseEvent<HTMLButtonElement>) => {
        if (editorRef === undefined || editorRef === null || editorRef.current === null) {
            return
        }
        const editorHandler: MetalNodeEditorHandler = editorRef.current
        editorHandler.load(props.data)
    }

    return (
        <Badge color="secondary" badgeContent={"?"}>
            <div>
                {nodeView.inputHandle(props.data)}
                <Box sx={{flexGrow: 0, paddingLeft: "1em", paddingRight: "0em"}} component={Paper}>
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
                            <Button size="small" sx={{borderTopLeftRadius: 0}} onClick={onEdit}><VscExpandAll/></Button>
                            <Button size="small" sx={{borderBottomLeftRadius: 0}} onClick={onDelete}><AiOutlineDelete/></Button>
                        </Stack>
                    </Stack>
                </Box>
                {nodeView.outputHandle(props.data)}
            </div>
        </Badge>


    )
}

export const MetalNodeTypes = {
    metal: MetalNode
}