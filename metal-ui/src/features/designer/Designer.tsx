import {useEffect, useRef} from "react";
import {Graph, Node, Shape} from "@antv/x6";

export function Designer() {
    const container = useRef<HTMLDivElement>(null)

    useEffect(() => {
        if (!container.current) {
            return
        }

        const graph = new Graph({
            container: container.current,
            width: 200,
            height: 200
        });

        const node = new Shape.Rect({
            id: 'node01',
            x: 100,
            y: 200,
            width: 80,
            height: 40,
            angle: 30,
            attrs: {
                body: {
                    fill: 'blue',
                },
                label: {
                    text: 'Hello',
                    fill: 'white',
                },
            },
        })

        // const node = new Node({
        //     id: 'node01',
        //     x: 10,
        //     y: 10,
        //     width: 10,
        //     height: 10,
        //     shape: 'rect'
        // });

        //
        graph.addNode(node)

        return () => {
            graph.dispose()
        };
    }, [])

    return (
        <div ref={container}></div>
    )
}