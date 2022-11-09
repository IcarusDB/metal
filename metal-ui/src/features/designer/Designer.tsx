import {MouseEvent as ReactMouseEvent, useCallback, useMemo, useRef} from "react";
import ReactFlow, {
    addEdge,
    Background,
    Connection,
    Controls,
    Edge,
    updateEdge,
    FitViewOptions,
    Node,
    OnConnect,
    useEdgesState,
    useNodesState, useReactFlow, ControlButton, MiniMap
} from 'reactflow';
import 'reactflow/dist/style.css';
import {MetalNodeProps, MetalNodeTypes, MetalViewIcons, onConnectValid} from "./MetalView";
import {Metal, Metals, MetalTypes} from "../../model/Metal";
import {VscDebugStart, VscDebugStop} from "react-icons/vsc";
import {CgRadioChecked} from "react-icons/cg";
import {AiOutlineDeploymentUnit} from "react-icons/ai";

const sourceNode: MetalNodeProps = {
    type: MetalTypes.SOURCE,
    onDelete: ()=>{},
    onUpdate: (newMetal: Metal) => {},
    metal: {
        id: 'node-0',
        name: 'node-0',
        props: {}
    },
    metalPkg: {
        pkg: "org.metal:metal-on-spark-extensions:1.0.0-SNAPSHOT",
        class: "org.metal.backend.spark.extension.JsonFileMSource",
        formSchema: {
            "type": "object",
            "id": "urn:jsonschema:org:metal:backend:spark:extension:IJsonFileMSourceProps",
            "properties": {
                "schema": {
                    "type": "string"
                },
                "path": {
                    "type": "string"
                }
            }
        }
    }
}

const sinkNode: MetalNodeProps = {
    type: MetalTypes.SINK,
    onDelete: ()=>{},
    onUpdate: (newMetal: Metal) => {},
    metal: {
        id: 'node-1',
        name: 'node-1',
        props: {}
    },
    metalPkg: {
        "pkg": "org.metal:metal-on-spark-extensions:1.0.0-SNAPSHOT",
        "class": "org.metal.backend.spark.extension.ConsoleMSink",
        "formSchema": {
            "type": "object",
            "id": "urn:jsonschema:org:metal:backend:spark:extension:IConsoleMSinkProps",
            "properties": {
                "numRows": {
                    "type": "integer"
                }
            }
        }
    }
}

const mapperNode: MetalNodeProps = {
    type: MetalTypes.MAPPER,
    onDelete: ()=>{},
    onUpdate: (newMetal: Metal) => {},
    metal: {
        id: 'node-2',
        name: 'node-2',
        props: {}
    },
    metalPkg: {
        "pkg": "org.metal:metal-on-spark-extensions:1.0.0-SNAPSHOT",
        "class": "org.metal.backend.spark.extension.SqlMMapper",
        "formSchema": {
            "type": "object",
            "id": "urn:jsonschema:org:metal:backend:spark:extension:ISqlMMapperProps",
            "properties": {
                "tableAlias": {
                    "type": "string"
                },
                "sql": {
                    "type": "string"
                }
            }
        }
    }
}

const fusionNode: MetalNodeProps = {
    type: MetalTypes.FUSION,
    onDelete: ()=>{},
    onUpdate: (newMetal: Metal) => {},
    metal: {
        id: 'node-3',
        name: 'node-3',
        props: {}
    },
    metalPkg: {
        "pkg": "org.metal:metal-on-spark-extensions:1.0.0-SNAPSHOT",
        "class": "org.metal.backend.spark.extension.SqlMFusion",
        "formSchema": {
            "type": "object",
            "id": "urn:jsonschema:org:metal:backend:spark:extension:ISqlMFusionProps",
            "properties": {
                "tableAlias": {
                    "type": "object",
                    "additionalProperties": {
                        "type": "string"
                    }
                },
                "sql": {
                    "type": "string"
                }
            }
        }
    }
}


export function Designer() {
    const nodeTypes = useMemo(()=>({...MetalNodeTypes}), [])
    const counter = useRef<number>(0)

    const initialNodes: Node<MetalNodeProps>[] = []

    const initialEdges: Edge[] = []

    const fitViewOptions: FitViewOptions = {
        padding: 0.2
    }

    const[nodes, setNodes, onNodesChange] = useNodesState(initialNodes)
    const[edges, setEdges, onEdgesChange] = useEdgesState(initialEdges)

    const onConnect: OnConnect = useCallback(
        (connection: Connection ) => {
            if (!onConnectValid(connection, nodes, edges)) {
                return
            }
            setEdges((edges) => {
                return addEdge(connection, edges)
            })
        }, [nodes, edges])

    const onEdgeDoubleClick = useCallback((event: ReactMouseEvent, edge: Edge) => {
        setEdges((prevEdges: Edge[]) => {
            return prevEdges.filter(prevEdge => (edge.id !== prevEdge.id))
        })
    }, [])

    const onAddNode = useCallback((nodeTmpl: MetalNodeProps)=>{
        setNodes((prevNodes: Node<MetalNodeProps>[]) => {
            const id = counter.current++
            const nodeId = `node-${id}`
            const nodeCopy = {
                ...nodeTmpl,
                metal: {
                    id: nodeId,
                    name: `node-${id}`,
                    props: {}
                },
                onDelete: ()=>{
                    setNodes((prevNds: Node<MetalNodeProps>[]) => {
                        return prevNds.filter(nd => (nd.id !== nodeId))
                    })
                },
            }
            return prevNodes.concat({
                id: nodeCopy.metal.id,
                data: nodeCopy,
                type: "metal",
                position: {x: 5, y: 5}
            })
        })
    }, [])


    return (
        <div style={{ height: '100%' }}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onNodeContextMenu={(event: ReactMouseEvent, node: Node)=>{}}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onEdgeDoubleClick={onEdgeDoubleClick}
                fitView
                fitViewOptions={fitViewOptions}
                nodeTypes={nodeTypes}
            >
                <Background />
                <Controls>
                    <ControlButton onClick={()=>{onAddNode(sourceNode)}}>
                        {MetalViewIcons.SOURCE}
                    </ControlButton>
                    <ControlButton onClick={()=>{onAddNode(sinkNode)}}>
                        {MetalViewIcons.SINK}
                    </ControlButton>
                    <ControlButton onClick={()=>{onAddNode(mapperNode)}}>
                        {MetalViewIcons.MAPPER}
                    </ControlButton>
                    <ControlButton onClick={()=>{onAddNode(fusionNode)}}>
                        {MetalViewIcons.FUSION}
                    </ControlButton>
                </Controls>
                <Controls showZoom={false} showFitView={false} showInteractive={false} position={'top-right'}>
                    <ControlButton><AiOutlineDeploymentUnit/></ControlButton>
                    <ControlButton><CgRadioChecked/></ControlButton>
                    <ControlButton><VscDebugStart/></ControlButton>
                    <ControlButton><VscDebugStop/></ControlButton>
                </Controls>
                <MiniMap></MiniMap>
            </ReactFlow>
        </div>
    )
}