import {Graph} from "@antv/x6";

export class MetalsContext {
    private readonly _graph: Graph


    constructor(graph: Graph) {
        this._graph = graph;
    }


    public get graph(): Graph {
        return this._graph;
    }
}