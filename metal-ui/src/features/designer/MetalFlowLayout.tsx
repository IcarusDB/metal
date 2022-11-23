import { Node, Edge } from "reactflow";
import { MetalNodeProps } from "./MetalView";
import ELK, { ElkNode } from 'elkjs/lib/elk.bundled.js';

export async function layout(nodes: Node<MetalNodeProps>[], edges: Edge<any>[]) {
    const elk = new ELK()
    const graph = {
        id: "root",
        layoutOptions: { 
            "ekl.algorithm": "layered",
            "elk.direction": "DOWN",
            "org.eclipse.elk.layered.spacing.edgeNodeBetweenLayers": "80",
            "org.eclipse.elk.spacing.edgeNode": "80",
            "org.eclipse.elk.spacing.nodeNode": "80",
        },
        children: nodes.map(node => ({
            id: node.id,
            width: node.width === null || node.width === undefined? 0: node.width,
            height: node.height === null || node.height === undefined? 0: node.height
        })),
        edges: edges.map(edge => ({
            id: edge.id,
            sources: [edge.source],
            targets: [edge.target],
        }))
    }

    return elk.layout(graph).then(newGraph => {
        let newNodesMap = new Map<string, ElkNode>()
        if (newGraph.children === undefined) {
            return nodes;
        }

        newGraph.children.forEach(node => {
            newNodesMap.set(node.id, node)
        })

        return nodes.map(node => {
            const newNode = newNodesMap.get(node.id)
            if (newNode === undefined || newNode.x === undefined || newNode.y === undefined) {
                return node
            }
            return {
                ...node,
                position: {
                    x: newNode.x,
                    y: newNode.y,
                }
            }
        })
    })
}