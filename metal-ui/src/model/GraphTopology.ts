export class GraphTopology {
    private topology: Map<string, Set<string>>
    private inDegrees: Map<string, number>
    private outDegrees: Map<string, number>

    constructor(nodes: Set<string>, edges: {source: string, target: string}[]) {
        this.topology = new Map<string, Set<string>>()
        this.inDegrees = new Map<string, number>()
        this.outDegrees = new Map<string, number>()

        nodes.forEach(node => {
            this.topology.set(node, new Set<string>())
            this.inDegrees.set(node, 0)
            this.outDegrees.set(node, 0)
        })
        edges.forEach(edge =>{
            const source = edge.source
            const target = edge.target
            const targets = this.topology.get(source)
            if (targets !== undefined) {
                targets.add(target)
            }

            const inDegree = this.inDegrees.get(target)
            const outDegree = this.outDegrees.get(source)
            this.inDegrees.set(target, inDegree === undefined?0 :inDegree + 1)
            this.outDegrees.set(source, outDegree === undefined?0 :outDegree + 1)
        })
    }

    public inDegree(node: string): number {
        const inDegree = this.inDegrees.get(node)
        return inDegree === undefined?0 :inDegree
    }

    public outDegree(node: string): number {
        const outDegree = this.outDegrees.get(node)
        return outDegree === undefined?0 :outDegree
    }

    public next(node: string): Set<string> {
        if (!this.topology.has(node) || this.topology.get(node) === undefined) {
            return new Set<string>()
        }
        const queue: Array<string> = new Array<string>()
        const starter = this.topology.get(node)
        if (starter === undefined) {
            return new Set<string>()
        }
        queue.push(...Array.from<string>(starter))
        const next = new Set<string>()
        while (queue.length !== 0) {
            const current = queue.pop()
            if (current === undefined) {
                break
            }
            if (next.has(current)) {
                continue
            }
            next.add(current)
            const targets = this.topology.get(current)
            if (targets === undefined) {
                continue
            }
            queue.push(...Array.from<string>(targets))
        }

        return next
    }
}