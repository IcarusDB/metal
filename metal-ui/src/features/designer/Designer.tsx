import {useEffect, useCallback, useRef} from "react";
import ReactFlow, {
    Controls,
    Background,
    Node,
    Edge,
    FitViewOptions,
    useNodesState,
    useEdgesState,
    addEdge,
    OnConnect, Connection
} from 'reactflow';
import 'reactflow/dist/style.css';


export function Designer() {
    const initialNodes: Node[] = [{
        id: 'node-0',
        data: {label: 'node-0'},
        position: {x: 5, y: 5}
    }, {
        id: 'node-1',
        data: {label: 'node-1'},
        position: {x: 15, y: 15}
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

    const onConnect: OnConnect = useCallback((connection: Connection ) => {
        setEdges(addEdge(connection, edges))
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
            >
                <Background />
                <Controls />
            </ReactFlow>
        </div>
    )
}