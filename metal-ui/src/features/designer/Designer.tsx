import { MouseEvent as ReactMouseEvent, useCallback, useEffect, useMemo, useRef } from "react";
import ReactFlow, {
    addEdge,
    getIncomers,
    getOutgoers,
    Background,
    Connection,
    Controls,
    Edge,
    FitViewOptions,
    Node,
    OnConnect,
    useEdgesState,
    useNodesState,
    ControlButton,
    MiniMap,
} from "reactflow";
import "reactflow/dist/style.css";
import {
    MetalNodeProps,
    MetalNodeTypes,
    onConnectValid,
    MetalNodeInOutUtil,
} from "./MetalView";
import { Metal } from "../../model/Metal";
import { VscDebugStart, VscDebugStop } from "react-icons/vsc";
import { CgRadioChecked } from "react-icons/cg";
import { AiOutlineDeploymentUnit } from "react-icons/ai";
import { Paper, Stack } from "@mui/material";
import { MetalNodeEditor, MetalNodeEditorHandler } from "./MetalNodeEditor";
import { MetalExplorer } from "./explorer/MetalExplorer";
import { Box } from "@mui/system";


export function Designer() {
    const nodeTypes = useMemo(() => ({ ...MetalNodeTypes }), []);
    const counter = useRef<number>(0);
    const nodeEditorRef = useRef<MetalNodeEditorHandler>(null);
    const nodeInOutRef = useRef<MetalNodeInOutUtil>(MetalNodeInOutUtil.default());

    const initialNodes: Node<MetalNodeProps>[] = [];

    const initialEdges: Edge<any>[] = [];

    const fitViewOptions: FitViewOptions = {
        padding: 0.2,
    };

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    const inputs = useCallback(
        (id: string) => {
            return nodes
                .filter((node: Node<MetalNodeProps>) => node.id === id)
                .flatMap((node: Node<MetalNodeProps>) =>
                    getIncomers<MetalNodeProps, MetalNodeProps>(node, nodes, edges)
                );
        },
        [nodes, edges]
    );

    const outputs = useCallback(
        (id: string) => {
            return nodes
                .filter((node: Node<MetalNodeProps>) => node.id === id)
                .flatMap((node: Node<MetalNodeProps>) =>
                    getOutgoers<MetalNodeProps, MetalNodeProps>(node, nodes, edges)
                );
        },
        [nodes, edges]
    );

    const onConnect: OnConnect = useCallback(
        (connection: Connection) => {
            if (!onConnectValid(connection, nodes, edges)) {
                return;
            }
            setEdges((edges) => {
                return addEdge(connection, edges);
            });
            // eslint-disable-next-line react-hooks/exhaustive-deps
        },
        [nodes, edges]
    );

    const onEdgeDoubleClick = useCallback((event: ReactMouseEvent, edge: Edge) => {
        setEdges((prevEdges: Edge[]) => {
            return prevEdges.filter((prevEdge) => edge.id !== prevEdge.id);
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const updateNodeMetal = useCallback((newMetal: Metal) => {
        setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            return prevNodes.map((prevNode: Node<MetalNodeProps>) => {
                if (prevNode.data.metal.id !== newMetal.id) {
                    return prevNode;
                }
                return {
                    ...prevNode,
                    data: {
                        ...prevNode.data,
                        metal: newMetal,
                    },
                };
            });
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const onAddNode = useCallback((nodeTmpl: MetalNodeProps) => {
        setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            const id = counter.current++;
            const nodeId = `node-${id}`;
            const nodeCopy = {
                ...nodeTmpl,
                metal: {
                    id: nodeId,
                    name: `node-${id}`,
                    props: {},
                },
                onUpdate: updateNodeMetal,
                onDelete: () => {
                    setNodes((prevNds: Node<MetalNodeProps>[]) => {
                        return prevNds.filter((nd) => nd.id !== nodeId);
                    });
                    setEdges((prevEdges: Edge[]) => {
                        return prevEdges.filter(
                            (edge) => !(edge.source === nodeId || edge.target === nodeId)
                        );
                    });
                },
                editorRef: nodeEditorRef,
            };
            return prevNodes.concat({
                id: nodeCopy.metal.id,
                data: nodeCopy,
                type: "metal",
                position: { x: 5, y: 5 },
            });
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);


    useEffect(() => {
        nodeInOutRef.current.update({
            inputs: inputs,
            outputs: outputs,
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [nodes, edges]);

    return (
        <div className="panel">
            <Stack 
                direction="row" 
                justifyContent="center" 
                alignItems="flex-start" 
                spacing={2}
                sx={{height:"100%", width:"100%"}}
            >
                <Box component={Paper} sx={{ height: "100%", width: "75%" }}>
                    <ReactFlow
                        nodes={nodes}
                        edges={edges}
                        onNodesChange={onNodesChange}
                        onNodeContextMenu={(event: ReactMouseEvent, node: Node) => {}}
                        onEdgesChange={onEdgesChange}
                        onConnect={onConnect}
                        onEdgeDoubleClick={onEdgeDoubleClick}
                        fitView
                        fitViewOptions={fitViewOptions}
                        nodeTypes={nodeTypes}
                    >
                        <Background />
                        <Controls/>
                        <Controls
                            showZoom={false}
                            showFitView={false}
                            showInteractive={false}
                            position={"top-right"}
                        >
                            <ControlButton>
                                <AiOutlineDeploymentUnit />
                            </ControlButton>
                            <ControlButton>
                                <CgRadioChecked />
                            </ControlButton>
                            <ControlButton>
                                <VscDebugStart />
                            </ControlButton>
                            <ControlButton>
                                <VscDebugStop />
                            </ControlButton>
                        </Controls>
                        <MiniMap></MiniMap>
                    </ReactFlow>
                </Box>
                <Box component={Paper} sx={{height:"100%", width:"25%"}}>
                    <MetalExplorer addNode={onAddNode}/>
                </Box>
            </Stack>

            <MetalNodeEditor ref={nodeEditorRef} metalNodeInOutRef={nodeInOutRef} />
        </div>
    );
}
