import { Skeleton } from "@mui/material";
import _ from "lodash";
import {
    MouseEvent as ReactMouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { VscTypeHierarchy } from "react-icons/vsc";
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
    BackgroundVariant,
} from "reactflow";
import { useAsync } from "../../api/Hooks";
import { Metal, MetalTypes } from "../../model/Metal";
import { Spec } from "../../model/Spec";
import { IReadOnly } from "../ui/Commons";
import { HotNode } from "./DesignerActionSlice";
import { useBackendStatusFn, useFlowPending, useHotNodesFn, useMetalFlowFn, useMetalNodeEditor, useModifyFn, useSpecFlow } from "./DesignerProvider";
import { useFlowLayout } from "./MetalFlowLayout";
import { MetalNodeProps, MetalNodeState, MetalNodeTypes, onConnectValid } from "./MetalView";
import { SpecFlow } from "./SpecLoader";

enum LoadState {
    UNLOAD,
    LOADING,
    LAYOUTING,
    LOADED,
}


export interface MetalFlowProps extends IReadOnly{
    flow?: SpecFlow;
}

export const MetalFlow = (props: MetalFlowProps) => {
    const nodeTypes = useMemo(() => ({ ...MetalNodeTypes }), []);
    const counter = useRef<number>(0);
    const { isReadOnly} = props;
    const [flow] = useSpecFlow();
    const [,setMetalFlowAction] = useMetalFlowFn();
    const [nodeEditorAction] = useMetalNodeEditor();
    const [,,onHotNodesChange] = useHotNodesFn();
    const [getBackendStatus] = useBackendStatusFn();
    const [,modify] = useModifyFn();
    const flowInstance = useReactFlow();
    const [isPending,] = useFlowPending();
    
    const [loadStatus, setLoadStatus] = useState<LoadState>(LoadState.UNLOAD);
    const nodePropsWrap = useCallback(
        (nodeProps: MetalNodeProps) => ({
            ...nodeProps,
            editor: nodeEditorAction,
        }),
        [nodeEditorAction]
    );

    const [run] = useAsync<void>();

    const fitViewOptions: FitViewOptions = {
        padding: 1,
    };

    const broadCastNodeStatus = useCallback((nodes: string[], status: MetalNodeState)=>{
        const nds: Node<MetalNodeProps>[] = flowInstance.getNodes();
        const edges: Edge<any>[] = flowInstance.getEdges();
        const queue = new Array<Node<MetalNodeProps>>();
        const visited = new Set<string>();

        nodes.forEach((node: string) => {
            const nd: Node<MetalNodeProps> | undefined = flowInstance.getNode(node);
            if (nd !== undefined) {
                queue.push(nd);
            }
        });

        while(queue.length > 0) {
            const nd = queue.pop();
            if (nd === undefined) {
                break;
            }

            visited.add(nd.id);
            const nexts: Node<MetalNodeProps>[] = getOutgoers(nd, nds, edges);
            nexts.forEach(nd => {
                queue.push(nd);
            })
        }

        return Array.from(visited).map(nd => {
            const nde: HotNode = [nd, status, undefined]
            return nde;
        });
    }, [flowInstance]);

    const setNodesStatus = useCallback((nds: HotNode[]) => {
        const unAnalysisNds = nds.filter(nd => nd[1] === MetalNodeState.UNANALYSIS).map(nd => nd[0]);
        const unAnalysised = broadCastNodeStatus(unAnalysisNds, MetalNodeState.UNANALYSIS);
        const mixNds = nds.map(nd => {
            if (nd[1] === MetalNodeState.ERROR) {
                return nd;
            }
            const unNd = unAnalysised.find((un) => (un[0] === nd[0]))
            return unNd? unNd: nd;
        });


        flowInstance.setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            return prevNodes.map(node => {
                const nd = mixNds.find(nde => nde[0] === node.id);
                return nd === undefined? node: {
                    ...node,
                    data: {
                        ...node.data,
                        status: nd[1],
                        msg: nd[2],
                    }
                }
            })
        })
    }, [broadCastNodeStatus, flowInstance]);

    const onConnect: OnConnect = useCallback(
        (connection: Connection) => {
            if (!onConnectValid(connection, flowInstance.getNodes(), flowInstance.getEdges())) {
                return;
            }
            if (connection.target !== null) {
                setNodesStatus([[connection.target, MetalNodeState.UNANALYSIS, undefined]]);
                modify(true);
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
        [flowInstance, modify, setNodesStatus]
    );

    const deleteEdge = useCallback(
        (edge: Edge) => {
            flowInstance.deleteElements({
                edges: [edge],
            });
            setNodesStatus([[edge.target, MetalNodeState.UNANALYSIS, undefined]])
            modify(true);
        },
        [flowInstance, modify, setNodesStatus]
    );

    const onEdgeDoubleClick = useCallback(
        (event: ReactMouseEvent, edge: Edge) => {
            deleteEdge(edge);
        },
        [deleteEdge]
    );

    const allNodes: () => Node<MetalNodeProps>[] = useCallback(() => (
        flowInstance.getNodes().map(nd => {
            const ndm: Node<MetalNodeProps> = nd;
            return ndm;
        })
    ), [flowInstance])

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

            const unAnalysed = new Set(broadCastNodeStatus([id], MetalNodeState.UNANALYSIS).map((nde) => (nde[0])));

            const newNode: Node<MetalNodeProps> = {
                ...node,
                data: {
                    ...node.data,
                    metal: newMetal,
                    status: MetalNodeState.UNANALYSIS,
                },
            };
            flowInstance.setNodes((prevNodes: Node<MetalNodeProps>[]) => {
                return prevNodes.map((prevNode: Node<MetalNodeProps>) => {
                    if (prevNode.id !== id) {
                        if (unAnalysed.has(prevNode.id)) {
                            return {
                                ...prevNode,
                                data: {
                                    ...prevNode.data,
                                    status: MetalNodeState.UNANALYSIS
                                }
                            }
                        }
                        return prevNode;
                    }
                    return newNode;
                });
            });
            modify(true);
        },
        [broadCastNodeStatus, flowInstance, modify]
    );

    const deleteNode = useCallback(
        (id: string) => {
            const willDeleteEdges = flowInstance
                .getEdges()
                .filter((edge) => edge.source === id || edge.target === id);

            const hotNodes: HotNode[] = flowInstance.getEdges()
                                        .filter((edge) => edge.source === id)
                                        .map((edge) => [edge.target, MetalNodeState.UNANALYSIS, undefined]);
            
            setNodesStatus(hotNodes);
            
            flowInstance.deleteElements({
                nodes: [{ id: id }],
                edges: willDeleteEdges,
            });
            modify(true);
        },
        [flowInstance, modify, setNodesStatus]
    );

    const addNode = useCallback(
        (nodeTmpl: MetalNodeProps) => {
            const allNodeIds = new Set(flowInstance.getNodes().map((node) => node.id));
            let nodeId = `node_${counter.current++}`;
            while (allNodeIds.has(nodeId)) {
                nodeId = `node_${counter.current++}`;
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
                inputs: nodeTmpl.type === MetalTypes.SOURCE || nodeTmpl.type === MetalTypes.SETUP? (id: string) => ([]): inputs,
                outputs: nodeTmpl.type === MetalTypes.SINK || nodeTmpl.type === MetalTypes.SETUP? (id: string) => ([]): outputs,    
                backendStatus: getBackendStatus,
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
            modify(true);
        },
        [deleteNode, flowInstance, getBackendStatus, inputs, isReadOnly, modify, nodePropsWrap, outputs, updateNode]
    );

    const [layout, layoutStatus] = useFlowLayout({
        onSuccess: (newNodes) => {
            flowInstance.setNodes(newNodes);
            setLoadStatus(LoadState.LOADED);
        },
    });

    const autoLayout = useCallback(() => {
        layout(flowInstance.getNodes, flowInstance.getEdges);
    }, [flowInstance.getEdges, flowInstance.getNodes, layout]);

    const onLayout = useCallback(() => {
        autoLayout();
    }, [autoLayout]);

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
                    inputs: nodeTmpl.type === MetalTypes.SOURCE || nodeTmpl.type === MetalTypes.SETUP? (id: string) => ([]): inputs,
                    outputs: nodeTmpl.type === MetalTypes.SINK || nodeTmpl.type === MetalTypes.SETUP? (id: string) => ([]): outputs,
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
        [deleteNode, inputs, isReadOnly, nodePropsWrap, outputs, updateNode]
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
            loadFlow(newFlow);
            // setLoadStatus(LoadState.LOADING);
        },
        [loadFlow]
    );

    const exportSpec = useCallback(()=>{
        const nodes: Node<MetalNodeProps>[] = flowInstance.getNodes().map(nd => {
            const ndm: Node<MetalNodeProps> = nd;
            return ndm;
        });
        const edges: Edge<any>[] = flowInstance.getEdges();
        const spec: Spec = {
            version: "1.0",
            metals: nodes.map(node => node.data.metal),
            edges: edges.map(edge => ({left: edge.source, right: edge.target})),
            waitFor: []
        }
        return spec;
    }, [flowInstance]); 

    const checkSpec = useCallback((spec: Spec) => {
        const emptyMetals = spec.metals.filter(metal => (metal.props === undefined || metal.props === null || _.isEmpty(metal.props)));
        const inputsIllegalMetals = spec.metals.filter(metal => (
            !(metal.type === MetalTypes.SETUP || metal.type === MetalTypes.SOURCE)
        )).map(metal => ({
            metal: metal,
            inputs: inputs(metal.id).length,
        })).filter(({metal, inputs}) => (
            ((metal.type === MetalTypes.MAPPER || metal.type === MetalTypes.SINK) && inputs !== 1) ||
            (metal.type === MetalTypes.FUSION && inputs < 2)
        )).map(({metal, inputs}) => ({metal: metal.id, inputs: inputs}));
        return {
            emptyMetals: emptyMetals,
            inputsIllegalMetals: inputsIllegalMetals,
        }
    }, [inputs]);

    const exportSubSpec = useCallback(
        (target: string, isContainTarget: boolean) => {
            const targetNd: Node<MetalNodeProps> | undefined = flowInstance.getNode(target);
            if (targetNd === undefined) {
                return undefined;
            }
            const nodes: Node<MetalNodeProps>[] = flowInstance.getNodes();
            const edges: Edge<any>[] = flowInstance.getEdges();
            const queue = new Array<Node<MetalNodeProps>>();
            const visitedEdges = new Array<[string, string]>();
            const visited = new Set<Node<MetalNodeProps>>();
            if (isContainTarget) {
                queue.push(targetNd);
            } else {
                getIncomers(targetNd, nodes, edges).forEach(incomer => queue.push(incomer));
            }
            
            while (queue.length !== 0) {
                const nd = queue.pop();
                if (nd === undefined) {
                    break;
                }
                const incomers: Node<MetalNodeProps>[] = getIncomers(nd, nodes, edges);
                incomers.forEach((incomer) => visitedEdges.push([incomer.id, nd?.id]));
                incomers.forEach((incomer) => queue.push(incomer));
                visited.add(nd);
            }

            const spec: Spec = {
                version: "1.0",
                metals: _.uniqWith(Array.from(visited), (a, b) => a.id === b.id).map(
                    (node: Node<MetalNodeProps>) => node.data.metal
                ),
                edges: visitedEdges.map((edge) => ({ left: edge[0], right: edge[1] })),
                waitFor: [],
            };
            return spec;
        },
        [flowInstance]
    ); 

    useMemo(() => {
        setMetalFlowAction({
            allNodes: allNodes,
            inputs: inputs,
            outputs: outputs,
            addNode: addNode,
            load: load,
            export: exportSpec,
            exportSubSpec: exportSubSpec,
            checkSpec: checkSpec,
        });
    }, [addNode, allNodes, checkSpec, exportSpec, exportSubSpec, inputs, load, outputs, setMetalFlowAction]);

    const initialNodes = useMemo(() => loadNodesFromFlow(flow), []);
    const initialEdges = useMemo(() => loadEdgesFromFlow(flow), []);

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    useEffect(() => {
        switch (loadStatus) {
            case LoadState.UNLOAD:
                if (flow !== undefined) {
                    load(flow);
                    setLoadStatus(LoadState.LAYOUTING);
                }
                break;
            case LoadState.LAYOUTING:
                autoLayout();
            // case LoadState.LOADING:
            //     setTimeout(()=>{
            //         setLoadStatus(LoadState.LAYOUTING);
            //         autoLayout();
            //     }, 1000);
            //     break;
        }

        const unsub = onHotNodesChange((hotNodes) => {
            console.log(hotNodes);
            if (hotNodes === undefined) {
                return;
            }
            setNodesStatus(hotNodes);   
        })
        return unsub;
    }, [autoLayout, flow, load, loadStatus, onHotNodesChange, setNodesStatus]);

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
            onEdgeDoubleClick={isReadOnly || isPending? ()=>{}: onEdgeDoubleClick}
            fitView
            fitViewOptions={fitViewOptions}
            nodeTypes={nodeTypes}
        >
            <Background variant={isPending? BackgroundVariant.Lines: BackgroundVariant.Dots}/>
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
