import React, { ForwardedRef, forwardRef, MouseEvent as ReactMouseEvent, useCallback, useEffect, useImperativeHandle, useMemo, useRef } from "react";
import { AiOutlineDeploymentUnit } from "react-icons/ai";
import { CgRadioChecked } from "react-icons/cg";
import { VscDebugStart, VscDebugStop, VscTypeHierarchy } from "react-icons/vsc";
import {
    addEdge,
    Node,
    Edge,
    ReactFlow,
    ReactFlowProvider,
    FitViewOptions,
    OnConnect,
    Connection,
    Controls,
    Background,
    ControlButton,
    MiniMap,
    getIncomers,
    getOutgoers,
    useNodesState,
    useEdgesState,
    MarkerType,
} from "reactflow";
import { useAsync } from "../../api/Hooks";
import { Metal } from "../../model/Metal";
import { Spec } from "../../model/Spec";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { getAllMetalPkgsOfClasses } from "./explorer/MetalPkgApi";
import { layout } from "./MetalFlowLayout";
import { MetalNodeProps, MetalNodeTypes, onConnectValid } from "./MetalView";
import { SpecFlow } from "./SpecLoader";

export interface MetalFlowProps {
    nodePropsWrap: (node: MetalNodeProps) => MetalNodeProps,
    flow?: SpecFlow
}

export interface MetalFlowHandler {
    inputs: (id: string) => Node<MetalNodeProps>[],
    outputs: (id: string) => Node<MetalNodeProps>[],
    updateNodeMetal: (metal: Metal) => void,
    addNode: (nodeProps: MetalNodeProps) => void,
}

export const MetalFlow = forwardRef((props: MetalFlowProps, ref: ForwardedRef<MetalFlowHandler>) => {
    const nodeTypes = useMemo(() => ({ ...MetalNodeTypes }), []);
    const counter = useRef<number>(0);
    const {
        nodePropsWrap,
        flow,
    } = props;

    
    const initialNodes: Node<MetalNodeProps>[] = [];
    const initialEdges: Edge<any>[] = [];
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
    const {run} = useAsync<void>();

    const fitViewOptions: FitViewOptions = {
        padding: 1,
    };


    const onConnect: OnConnect = useCallback(
        (connection: Connection) => {
            if (!onConnectValid(connection, nodes, edges)) {
                return;
            }
            setEdges((edges) => {
                return addEdge(
                    {
                        ...connection,
                        markerEnd: { 
                            type: MarkerType.ArrowClosed,
                            color: "black",
                            width: 18,
                            height: 24,
                        },
                    },
                    edges
                );
            });
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [edges, nodes]
    );

    const onEdgeDoubleClick = useCallback((event: ReactMouseEvent, edge: Edge) => {
        setEdges((prevEdges: Edge[]) => {
            return prevEdges.filter((prevEdge) => edge.id !== prevEdge.id);
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const inputs = useCallback(
        (id: string) => {
            return nodes
                .filter((node: Node<MetalNodeProps>) => node.id === id)
                .flatMap((node: Node<MetalNodeProps>) =>
                    getIncomers<MetalNodeProps, MetalNodeProps>(node, nodes, edges)
                );
        },
        [edges, nodes]
    );

    const outputs = useCallback(
        (id: string) => {
            return nodes
                .filter((node: Node<MetalNodeProps>) => node.id === id)
                .flatMap((node: Node<MetalNodeProps>) =>
                    getOutgoers<MetalNodeProps, MetalNodeProps>(node, nodes, edges)
                );
        },
        [edges, nodes]
    );

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

    const addNode = useCallback((nodeTmpl: MetalNodeProps) => {
        setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            const id = counter.current++;
            const nodeId = `node-${id}`;
            const nodeProps = {
                ...nodeTmpl,
                metal: {
                    type: nodeTmpl.metalPkg.class,
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
            };
            const nodePropsWrapped = nodePropsWrap(nodeProps)
            return prevNodes.concat({
                id: nodePropsWrapped.metal.id,
                data: nodePropsWrapped,
                type: "metal",
                position: { x: 5, y: 5 },
            });
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const autoLayout = useCallback(() => {
        run(layout(nodes, edges).then(newNodes => setNodes(newNodes)))
    }, [edges, nodes, run, setNodes])



    useImperativeHandle(ref, ()=>({
        inputs: inputs,
        outputs: outputs,
        updateNodeMetal: updateNodeMetal,
        addNode: addNode
    }), [addNode, inputs, outputs, updateNodeMetal])
    
    useEffect(()=>{
        if (flow === undefined) {
            return;
        }
        setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            const nodes = [...prevNodes];
            flow.nodeTmpls.forEach((nodeTmpl: MetalNodeProps | undefined) => {
                if (nodeTmpl === undefined) {
                    return;
                }
                const nodeId = nodeTmpl.metal.id;
                const nodeProps = {
                    ...nodeTmpl,
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
                };
                const nodePropsWrapped = nodePropsWrap(nodeProps)
                nodes.push({
                    id: nodePropsWrapped.metal.id,
                    data: nodePropsWrapped,
                    type: "metal",
                    position: { x: 5, y: 5 },
                });
            })
            return nodes;
        });

        flow.connections.forEach(connection => {
            setEdges((edges) => {
                return addEdge(
                    {
                        ...connection,
                        markerEnd: { 
                            type: MarkerType.ArrowClosed,
                            color: "black",
                            width: 18,
                            height: 24,
                        },
                    },
                    edges
                );
            });
        });
    }, [flow])

    return (
<ReactFlowProvider>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onEdgeDoubleClick={onEdgeDoubleClick}
                fitView
                fitViewOptions={fitViewOptions}
                nodeTypes={nodeTypes}
            >
                <Controls />
                <Background />
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
                    <ControlButton onClick={autoLayout}>
                        <VscTypeHierarchy/>
                    </ControlButton>
                </Controls>
                <MiniMap></MiniMap>
            </ReactFlow>
        </ReactFlowProvider>
    );
})
