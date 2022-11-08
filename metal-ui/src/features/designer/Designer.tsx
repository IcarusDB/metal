import {useCallback, useMemo} from "react";
import ReactFlow, {
    addEdge,
    Background,
    Connection,
    Controls,
    Edge,
    FitViewOptions,
    Node,
    OnConnect,
    useEdgesState,
    useNodesState
} from 'reactflow';
import 'reactflow/dist/style.css';
import {MetalNodeProps, MetalNodeTypes} from "./MetalView";
import {MetalTypes} from "../../model/Metal";

const sourceNode: MetalNodeProps = {
    type: MetalTypes.SOURCE,
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

    const initialNodes: Node[] = [{
        id: sourceNode.metal.id,
        data: sourceNode,
        type: "metal",
        position: {x: 5, y: 5}
    }, {
        id: sinkNode.metal.id,
        data: sinkNode,
        type: "metal",
        position: {x: 5, y: 5}
    }, {
        id: mapperNode.metal.id,
        data: mapperNode,
        type: "metal",
        position: {x: 5, y: 5}
    }, {
        id: fusionNode.metal.id,
        data: fusionNode,
        type: "metal",
        position: {x: 5, y: 5}
    }]

    const initialEdges: Edge[] = [{
        id: '0-1',
        source: 'node-0',
        target: 'node-1',
        animated: true
    }]

    const fitViewOptions: FitViewOptions = {
        padding: 0.2
    }

    const[nodes, setNodes, onNodesChange] = useNodesState(initialNodes)
    const[edges, setEdges, onEdgesChange] = useEdgesState(initialEdges)

    const onConnect: OnConnect = useCallback(
        (connection: Connection ) => {
            setEdges((edges) => {
                return addEdge(connection, edges)
            })
        }, [])



    return (
        <div style={{ height: '100%' }}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                fitView
                fitViewOptions={fitViewOptions}
                nodeTypes={nodeTypes}
            >
                <Background />
                <Controls />
            </ReactFlow>
        </div>
    )
}