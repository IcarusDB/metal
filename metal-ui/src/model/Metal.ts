import {GraphTopology} from "./GraphTopology";

export enum MetalTypes {
    SOURCE = "source",
    SINK = "sink",
    MAPPER = "mapper",
    FUSION = "fusion",
    SETUP = "setup"
}

export interface Metal {
    id: string,
    name: string,
    props: any
}

export interface IMetal {
    hasInput: ()=>boolean;
    hasOutput: ()=>boolean;
    canAddInput: (graphTopology: GraphTopology, id: string)=>boolean;
}

export const MetalSource: IMetal = {
    hasInput: () => {
        return false
    },
    hasOutput: () => {
        return true
    },
    canAddInput: (graphTopology: GraphTopology, id: string) => {
        return false
    }
}

export const MetalSink: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return false
    },
    canAddInput: (graphTopology: GraphTopology, id: string) => {
        return graphTopology.inDegree(id) == 0
    }
}

export const MetalMapper: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return true
    },
    canAddInput: (graphTopology: GraphTopology, id: string) => {
        return graphTopology.inDegree(id) == 0
    }
}

export const MetalFusion: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return true
    },
    canAddInput: (graphTopology: GraphTopology, id: string) => {
        return true
    }
}

export const Metals = {
    metal: (type: string) => {
        switch (type) {
            case MetalTypes.SOURCE: {
                return MetalSource
            };
            case MetalTypes.SINK: {
                return MetalSink
            };
            case MetalTypes.MAPPER: {
                return MetalMapper
            };
            case MetalTypes.FUSION: {
                return MetalFusion
            };
            default: {
                return MetalSource
            }
        }
    }
}