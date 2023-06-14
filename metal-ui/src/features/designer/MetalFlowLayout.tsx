/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import { Node, Edge } from "reactflow";
import { MetalNodeProps } from "./MetalView";
import ELK, { ElkNode } from "elkjs/lib/elk.bundled.js";
import { IAsyncCallback, useAsync } from "../../api/Hooks";
import { useCallback } from "react";
import { State } from "../../api/State";

export async function layout(
  nodes: () => Node<MetalNodeProps>[],
  edges: () => Edge<any>[]
) {
  const elk = new ELK();
  const graph = {
    id: "root",
    layoutOptions: {
      "ekl.algorithm": "layered",
      "elk.direction": "DOWN",
      "org.eclipse.elk.layered.spacing.edgeNodeBetweenLayers": "180",
      "org.eclipse.elk.layered.spacing.nodeNodeBetweenLayers": "180",
      "org.eclipse.elk.spacing.edgeNode": "180",
      "org.eclipse.elk.spacing.nodeNode": "350",
      "org.eclipse.elk.partitioning.activate": "true",
    },
    children: nodes().map((node) => ({
      id: node.id,
      width: node.width === null || node.width === undefined ? 0 : node.width,
      height:
        node.height === null || node.height === undefined ? 0 : node.height,
    })),
    edges: edges().map((edge) => ({
      id: edge.id,
      sources: [edge.source],
      targets: [edge.target],
    })),
  };

  return elk.layout(graph).then((newGraph) => {
    let newNodesMap = new Map<string, ElkNode>();
    if (newGraph.children === undefined) {
      return nodes();
    }

    newGraph.children.forEach((node) => {
      newNodesMap.set(node.id, node);
    });

    return nodes().map((node) => {
      const newNode = newNodesMap.get(node.id);
      if (
        newNode === undefined ||
        newNode.x === undefined ||
        newNode.y === undefined
      ) {
        return node;
      }
      return {
        ...node,
        position: {
          x: newNode.x,
          y: newNode.y,
        },
      };
    });
  });
}

export function useFlowLayout(
  callback?: IAsyncCallback<Node<MetalNodeProps>[]>
): [
  (nodes: () => Node<MetalNodeProps>[], edges: () => Edge<any>[]) => void,
  State,
  Node<MetalNodeProps>[] | null
] {
  const [run, status, result] = useAsync<Node<MetalNodeProps>[]>(callback);
  const onLayout = useCallback(
    (nodes: () => Node<MetalNodeProps>[], edges: () => Edge<any>[]) => {
      run(layout(nodes, edges));
    },
    [run]
  );

  return [onLayout, status, result];
}
