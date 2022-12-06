import { ImUpload, ImDownload } from "react-icons/im";
import { AiOutlineFunction, AiOutlineDelete } from "react-icons/ai";
import { VscMerge, VscExpandAll, VscExtensions, VscPackage, VscSymbolClass } from "react-icons/vsc";
import { Connection, Node, Edge, NodeProps } from "reactflow";
import { Handle, Position } from "reactflow";
import { Box, Button, Paper, Stack, Badge, Divider, Typography, Container, Grid } from "@mui/material";
import { MouseEvent } from "react";
import { MetalPkg } from "../../model/MetalPkg";
import { Metal, Metals, MetalTypes } from "../../model/Metal";
import { GraphTopology } from "../../model/GraphTopology";
import { MetalNodeEditorHandler } from "./DesignerProvider";
import { IReadOnly } from "../ui/Commons";

export const MetalViewIcons = {
    SOURCE: <ImUpload />,
    SINK: <ImDownload />,
    MAPPER: <AiOutlineFunction />,
    FUSION: <VscMerge />,
    SETUP: <VscExtensions />,
};

export function metalViewIcon(type: MetalTypes) {
    switch (type) {
        case MetalTypes.SOURCE:
            return MetalViewIcons.SOURCE;
        case MetalTypes.SINK:
            return MetalViewIcons.SINK;
        case MetalTypes.MAPPER:
            return MetalViewIcons.MAPPER;
        case MetalTypes.FUSION:
            return MetalViewIcons.FUSION;
        default:
            return MetalViewIcons.SETUP;
    }
}

export interface MetalNodeProps extends IReadOnly {
    metalPkg: MetalPkg;
    metal: Metal;
    type: MetalTypes;
    onUpdate: (newMetal: Metal) => void;
    onDelete: () => void;
    editor?: MetalNodeEditorHandler;
}

export interface IMetalNodeView {
    inputHandle: (props: MetalNodeProps) => JSX.Element;
    outputHandle: (props: MetalNodeProps) => JSX.Element;
    logo: (props: MetalNodeProps) => JSX.Element;
}

const inputHandle = (props: MetalNodeProps) => {
    return <Handle type={"target"} id={`${props.metal.id}-input`} position={Position.Top}></Handle>;
};

const outputHandle = (props: MetalNodeProps) => {
    return (
        <Handle type={"source"} id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
    );
};

export const MetalSourceNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return <></>;
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SOURCE;
    },
};

export const MetalSinkNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return <></>;
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SINK;
    },
};

export const MetalMapperNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.MAPPER;
    },
};

export const MetalFusionNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.FUSION;
    },
};

export const MetalNodeViews = {
    metalNodeView: (type: string) => {
        switch (type) {
            case MetalTypes.SOURCE:
                return MetalSourceNodeView;
            case MetalTypes.SINK:
                return MetalSinkNodeView;
            case MetalTypes.FUSION:
                return MetalFusionNodeView;
            case MetalTypes.MAPPER:
                return MetalMapperNodeView;
            default:
                return MetalSourceNodeView;
        }
    },
};

export function onConnectValid(
    connection: Connection,
    nodes: Node<MetalNodeProps>[],
    edges: Edge<any>[]
) {
    if (connection.target === null || connection.source === null) {
        return false;
    }

    if (connection.target === connection.source) {
        return false;
    }

    const nodesOnGraph = new Set<string>(nodes.map((node) => node.id));
    const edgesOnGraph = edges.map((edge) => {
        return { source: edge.source, target: edge.target };
    });

    const graphTopology: GraphTopology = new GraphTopology(nodesOnGraph, edgesOnGraph);
    let isValid = true;
    const inputs: Node<MetalNodeProps>[] = nodes.filter((node) => node.id === connection.target);
    inputs.forEach((node) => {
        isValid =
            isValid &&
            Metals.metal(node.data.type).hasInput() &&
            Metals.metal(node.data.type).canAddInput(graphTopology, node.id);
    });
    if (!isValid) {
        return false;
    }

    const outputs: Node<MetalNodeProps>[] = nodes.filter((node) => node.id === connection.source);
    outputs.forEach((node) => {
        isValid = isValid && Metals.metal(node.data.type).hasOutput();
    });
    if (!isValid) {
        return false;
    }

    const next = graphTopology.next(connection.target);
    return !next.has(connection.source);
}

export function MetalNode(props: NodeProps<MetalNodeProps>) {
    const { isReadOnly, metal, metalPkg, type, onDelete, onUpdate } = props.data;
    const editor = props.data.editor;
    const nodeView: IMetalNodeView = MetalNodeViews.metalNodeView(type);

    const onEdit = (event: MouseEvent<HTMLAnchorElement> | MouseEvent<HTMLButtonElement>) => {
        if (editor === undefined) {
            return;
        }
        editor.load(props.data);
    };

    return (
        <Badge color="secondary" badgeContent={"?"}>
            <div>
                {nodeView.inputHandle(props.data)}
                <Paper square>
                    <div
                        style={{
                            display: "flex",
                            boxSizing: "border-box",
                            width: "100%",
                            flexDirection: "row",
                            flexWrap: "wrap",
                            alignContent: "center",
                            justifyContent: "space-around",
                            alignItems: "center",
                            wordBreak: "break-all",
                            paddingTop: "1vh",
                            paddingBottom: "1vh",
                            paddingRight: "1vw",
                            paddingLeft: "1vw",
                        }}
                    >
                        <div
                            style={{
                                display: "flex",
                                width: "100%",
                                flexDirection: "column",
                                flexWrap: "wrap",
                                alignContent: "flex-start",
                                justifyContent: "flex-start",
                                alignItems: "flex-start",
                            }}
                        >
                            <div
                                style={{
                                    display: "flex",
                                    width: "100%",
                                    flexDirection: "row",
                                    flexWrap: "nowrap",
                                    alignContent: "center",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                }}
                            >
                                <div
                                    style={{
                                        display: "flex",
                                        width: "100%",
                                        flexDirection: "row",
                                        flexWrap: "wrap",
                                        alignContent: "center",
                                        justifyContent: "flex-start",
                                        alignItems: "center",
                                    }}
                                >
                                    <div
                                        style={{
                                            fontSize: "2em",
                                            padding: "1vh",
                                        }}
                                    >
                                        {nodeView.logo(props.data)}
                                    </div>
                                    <Divider orientation="vertical" flexItem />
                                    <Typography
                                        variant={"h6"}
                                        sx={{
                                            paddingLeft: "1vw",
                                        }}
                                    >
                                        {metal.name}
                                    </Typography>
                                </div>
                                <div
                                    style={{
                                        display: "flex",
                                        flexDirection: "column",
                                        flexWrap: "wrap",
                                        alignContent: "center",
                                        justifyContent: "flex-end",
                                        alignItems: "center",
                                    }}
                                >
                                    <Button
                                        size="small"
                                        sx={{ borderTopLeftRadius: 0 }}
                                        onClick={onEdit}
                                    >
                                        <VscExpandAll />
                                    </Button>
                                    <Button
                                        size="small"
                                        sx={{ borderBottomLeftRadius: 0 }}
                                        onClick={onDelete}
                                        disabled={isReadOnly}
                                    >
                                        <AiOutlineDelete />
                                    </Button>
                                </div>
                            </div>

                            <Divider orientation="horizontal" flexItem />
                            <Grid container>
                                <Grid item xs={1}>
                                    <div 
                                        style={{
                                            fontSize: "1em",
                                        }}
                                    >
                                        <VscSymbolClass/>
                                    </div>
                                </Grid>
                                <Grid item xs={11}>
                                <Typography variant={"caption"} color={"GrayText"}>
                                {metalPkg.class}
                            </Typography>
                                </Grid>
                            </Grid>
                            
                        </div>
                    </div>
                </Paper>
                {nodeView.outputHandle(props.data)}
            </div>
        </Badge>
    );
}

export const MetalNodeTypes = {
    metal: MetalNode,
};
