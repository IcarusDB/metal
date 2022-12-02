import { Skeleton, Typography } from "@mui/material";
import {
    MouseEvent as ReactMouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { AiOutlineDeploymentUnit } from "react-icons/ai";
import { CgRadioChecked } from "react-icons/cg";
import { VscDebugStart, VscDebugStop, VscTypeHierarchy } from "react-icons/vsc";
import {
    addEdge,
    Node,
    Edge,
    ReactFlow,
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
    getRectOfNodes,
    useReactFlow,
} from "reactflow";
import { useAsync } from "../../api/Hooks";
import { Metal } from "../../model/Metal";
import { Mutable } from "../../model/Mutable";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { useAttableMetalFlow } from "./DesignerProvider";
import { layout } from "./MetalFlowLayout";
import { MetalNodeProps, MetalNodeTypes, onConnectValid } from "./MetalView";
import { SpecFlow } from "./SpecLoader";

enum LoadState {
    UNLOAD,
    LOADING,
    LAYOUTING,
    LOADED,
}

export interface MetalFlowHandler {
    inputs: (id: string) => Node<MetalNodeProps>[];
    outputs: (id: string) => Node<MetalNodeProps>[];
    addNode: (nodeProps: MetalNodeProps) => void;
    load: (newFlow: SpecFlow | undefined) => void;
}

export const metalFlowHandlerInitial: MetalFlowHandler = {
    inputs: (id: string) => [],
    outputs: (id: string) => [],
    addNode: (nodeProps: MetalNodeProps) => {},
    load: (newFlow: SpecFlow | undefined) => {},
};

export class MutableMetalFlowHandler extends Mutable<MetalFlowHandler> implements MetalFlowHandler {
    inputs(id: string) {
        return this.get().inputs(id);
    }

    outputs(id: string) {
        return this.get().outputs(id);
    }

    addNode(nodeProps: MetalNodeProps) {
        this.get().addNode(nodeProps);
    }

    load(newFlow: SpecFlow | undefined) {
        this.get().load(newFlow);
    }
}

export interface MetalFlowProps {
    nodePropsWrap: (node: MetalNodeProps) => MetalNodeProps;
    flow?: SpecFlow;
}

export const MetalFlow = (props: MetalFlowProps) => {
    const nodeTypes = useMemo(() => ({ ...MetalNodeTypes }), []);
    const counter = useRef<number>(0);
    const { nodePropsWrap, flow} = props;
    const handler = useAttableMetalFlow();
    const flowInstance = useReactFlow();
    const [loadStatus, setLoadStatus] = useState<LoadState>(LoadState.UNLOAD);

    const { run } = useAsync<void>();

    const fitViewOptions: FitViewOptions = {
        padding: 1,
    };

    const onConnect: OnConnect = useCallback(
        (connection: Connection) => {
            if (!onConnectValid(connection, flowInstance.getNodes(), flowInstance.getEdges())) {
                return;
            }
            flowInstance.setEdges((prevEdges) => {
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
                    prevEdges
                );
            });
        },
        [flowInstance]
    );

    const deleteEdge = useCallback(
        (edge: Edge) => {
            flowInstance.deleteElements({
                edges: [edge],
            });
        },
        [flowInstance]
    );

    const onEdgeDoubleClick = useCallback(
        (event: ReactMouseEvent, edge: Edge) => {
            deleteEdge(edge);
        },
        [deleteEdge]
    );

    const inputs = useCallback(
        (id: string) => {
            const node = flowInstance.getNode(id);
            if (node === undefined) {
                return [];
            }

            const inputNodes: Node<MetalNodeProps>[] = getIncomers(
                node,
                flowInstance.getNodes(),
                flowInstance.getEdges()
            );
            return inputNodes;
        },
        [flowInstance]
    );

    const outputs = useCallback(
        (id: string) => {
            const node = flowInstance.getNode(id);
            if (node === undefined) {
                return [];
            }

            const inputNodes: Node<MetalNodeProps>[] = getOutgoers(
                node,
                flowInstance.getNodes(),
                flowInstance.getEdges()
            );
            return inputNodes;
        },
        [flowInstance]
    );

    const updateNode = useCallback(
        (id: string, newMetal: Metal) => {
            const node = flowInstance.getNode(id);
            if (node === undefined) {
                return;
            }

            const newNode: Node<MetalNodeProps> = {
                ...node,
                data: {
                    ...node.data,
                    metal: newMetal,
                },
            };
            flowInstance.setNodes((prevNodes: Node<MetalNodeProps>[]) => {
                return prevNodes.map((prevNode: Node<MetalNodeProps>) => {
                    if (prevNode.id !== id) {
                        return prevNode;
                    }
                    return newNode;
                });
            });
        },
        [flowInstance]
    );

    const deleteNode = useCallback(
        (id: string) => {
            const willDeleteEdges = flowInstance
                .getEdges()
                .filter((edge) => edge.source === id || edge.target === id);

            flowInstance.deleteElements({
                nodes: [{ id: id }],
                edges: willDeleteEdges,
            });
        },
        [flowInstance]
    );

    const addNode = useCallback(
        (nodeTmpl: MetalNodeProps) => {
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
                onUpdate: (newMetal: Metal) => {
                    updateNode(nodeId, newMetal);
                },
                onDelete: () => {
                    deleteNode(nodeId);
                },
            };
            const nodePropsWrapped = nodePropsWrap(nodeProps);

            const viewport = flowInstance.getViewport();
            const rect = getRectOfNodes(flowInstance.getNodes());
            const node: Node<MetalNodeProps> = {
                id: nodePropsWrapped.metal.id,
                data: nodePropsWrapped,
                type: "metal",
                position: { x: viewport.x + rect.width / 2, y: viewport.y + rect.height / 2 },
            };
            flowInstance.addNodes(node);
        },
        [deleteNode, flowInstance, nodePropsWrap, updateNode]
    );

    const autoLayout = useCallback(() => {
        run(
            layout(flowInstance.getNodes, flowInstance.getEdges).then((newNodes) =>
                flowInstance.setNodes(newNodes)
            )
        );
    }, [flowInstance, run]);

    const onLayout = useCallback(() => {
        setLoadStatus(LoadState.LOADING);
    }, []);

    const loadNodesFromFlow = useCallback(
        (newFlow: SpecFlow | undefined) => {
            if (newFlow === undefined) {
                return [];
            }
            const newNodes: Node<MetalNodeProps>[] = [];
            newFlow.nodeTmpls.forEach((nodeTmpl: MetalNodeProps | undefined) => {
                if (nodeTmpl === undefined) {
                    return;
                }
                const nodeId = nodeTmpl.metal.id;
                const nodeProps = {
                    ...nodeTmpl,
                    onUpdate: (newMetal: Metal) => {
                        updateNode(nodeId, newMetal);
                    },
                    onDelete: () => {
                        deleteNode(nodeId);
                    },
                };
                const nodePropsWrapped = nodePropsWrap(nodeProps);
                newNodes.push({
                    id: nodePropsWrapped.metal.id,
                    data: nodePropsWrapped,
                    type: "metal",
                    position: { x: 5, y: 5 },
                });
            });
            return newNodes;
        },
        [deleteNode, nodePropsWrap, updateNode]
    );

    const loadEdgesFromFlow = useCallback((newFlow: SpecFlow | undefined) => {
        if (newFlow === undefined) {
            return [];
        }

        let newEdges: Edge<any>[] = [];
        newFlow.connections.forEach((connection) => {
            newEdges = addEdge(
                {
                    ...connection,
                    markerEnd: {
                        type: MarkerType.ArrowClosed,
                        color: "black",
                        width: 18,
                        height: 24,
                    },
                },
                newEdges
            );
        });
        return newEdges;
    }, []);

    const loadFlow = useCallback(
        (newFlow: SpecFlow | undefined) => {
            if (newFlow === undefined) {
                return;
            }
            const newNodes = loadNodesFromFlow(newFlow);

            let newEdges = loadEdgesFromFlow(newFlow);
            flowInstance.setNodes(newNodes);
            flowInstance.setEdges(newEdges);
        },
        [flowInstance, loadEdgesFromFlow, loadNodesFromFlow]
    );

    const load = useCallback(
        (newFlow: SpecFlow | undefined) => {
            if (load === undefined) {
                return;
            }

            loadFlow(newFlow);
            setLoadStatus(LoadState.LOADING);
        },
        [loadFlow]
    );

    useMemo(() => {
        handler.set({
            inputs: inputs,
            outputs: outputs,
            addNode: addNode,
            load: load,
        });
    }, [addNode, handler, inputs, load, outputs]);

    const initialNodes = useMemo(() => loadNodesFromFlow(flow), []);
    const initialEdges = useMemo(() => loadEdgesFromFlow(flow), []);

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    useEffect(() => {
        switch (loadStatus) {
            case LoadState.UNLOAD:
                if (flow !== undefined) {
                    load(flow);
                    setLoadStatus(LoadState.LOADING);
                }
                break;
            case LoadState.LOADING:
                autoLayout();
                setLoadStatus(LoadState.LAYOUTING);
                break;
            case LoadState.LAYOUTING:
                setLoadStatus(LoadState.LOADED);
                break;
        }
    }, [autoLayout, flow, load, loadStatus]);

    if (flow === undefined) {
        return <Skeleton></Skeleton>;
    }

    return (
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
                <ControlButton onClick={onLayout}>
                    <VscTypeHierarchy />
                </ControlButton>
            </Controls>
            <MiniMap></MiniMap>
        </ReactFlow>
    );
};
