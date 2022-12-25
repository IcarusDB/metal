import { Skeleton } from "@mui/material";
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
    useReactFlow,
} from "reactflow";
import { useAsync } from "../../api/Hooks";
import { Metal } from "../../model/Metal";
import { Spec } from "../../model/Spec";
import { IReadOnly } from "../ui/Commons";
import { useMetalFlow } from "./DesignerProvider";
import { layout } from "./MetalFlowLayout";
import { MetalNodeProps, MetalNodeTypes, onConnectValid } from "./MetalView";
import { SpecFlow } from "./SpecLoader";

enum LoadState {
    UNLOAD,
    LOADING,
    LAYOUTING,
    LOADED,
}


export interface MetalFlowProps extends IReadOnly{
    nodePropsWrap: (node: MetalNodeProps) => MetalNodeProps;
    flow?: SpecFlow;
}

export const MetalFlow = (props: MetalFlowProps) => {
    const nodeTypes = useMemo(() => ({ ...MetalNodeTypes }), []);
    const counter = useRef<number>(0);
    const { isReadOnly, nodePropsWrap, flow} = props;
    const [, setMetalFlowAction] = useMetalFlow();
    const flowInstance = useReactFlow();
    const [loadStatus, setLoadStatus] = useState<LoadState>(LoadState.UNLOAD);

    const [run] = useAsync<void>();

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
            const allNodeIds = new Set(flowInstance.getNodes().map((node) => node.id));
            let nodeId = `node-${counter.current++}`;
            while (allNodeIds.has(nodeId)) {
                nodeId = `node-${counter.current++}`;
            }

            const nodeProps = {
                ...nodeTmpl,
                metal: {
                    type: nodeTmpl.metalPkg.class,
                    id: nodeId,
                    name: nodeId,
                    props: {},
                },
                onUpdate: (newMetal: Metal) => {
                    updateNode(nodeId, newMetal);
                },
                onDelete: () => {
                    deleteNode(nodeId);
                },
                isReadOnly: isReadOnly,
            };
            const nodePropsWrapped = nodePropsWrap(nodeProps);

            const viewport = flowInstance.getViewport();
            const node: Node<MetalNodeProps> = {
                id: nodePropsWrapped.metal.id,
                data: nodePropsWrapped,
                type: "metal",
                position: { x: -viewport.x / viewport.zoom, y: -viewport.y / viewport.zoom },
            };
            flowInstance.addNodes(node);
        },
        [deleteNode, flowInstance, isReadOnly, nodePropsWrap, updateNode]
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
                    isReadOnly: isReadOnly,
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
        [deleteNode, isReadOnly, nodePropsWrap, updateNode]
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

    const exportSpec = useCallback(()=>{
        const nodes: Node<MetalNodeProps>[] = flowInstance.getNodes();
        const edges: Edge<any>[] = flowInstance.getEdges();
        const spec: Spec = {
            version: "1.0",
            metals: nodes.map(node => node.data.metal),
            edges: edges.map(edge => ({left: edge.source, right: edge.target})),
            waitFor: []
        }
        return spec;
    }, [flowInstance]);

    useMemo(() => {
        setMetalFlowAction({
            inputs: inputs,
            outputs: outputs,
            addNode: addNode,
            load: load,
            export: exportSpec,
        });
    }, [addNode, exportSpec, inputs, load, outputs, setMetalFlowAction]);

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
            onConnect={isReadOnly? undefined: onConnect}
            onEdgeDoubleClick={isReadOnly? ()=>{}: onEdgeDoubleClick}
            fitView
            fitViewOptions={fitViewOptions}
            nodeTypes={nodeTypes}
        >
            <Background />
            <Controls
                showZoom={true}
                showFitView={true}
                showInteractive={true}
                position={"top-right"}
            >
                <ControlButton 
                    onClick={onLayout}
                    title={"Layout"}
                >
                    <VscTypeHierarchy />
                </ControlButton>
            </Controls>
            <MiniMap></MiniMap>
        </ReactFlow>
    );
};
