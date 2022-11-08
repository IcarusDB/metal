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
}

export const MetalSource: IMetal = {
    hasInput: () => {
        return false
    },
    hasOutput: () => {
        return true
    }
}

export const MetalSink: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return false
    }
}

export const MetalMapper: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return true
    }
}

export const MetalFusion: IMetal = {
    hasInput: () => {
        return true
    },
    hasOutput: () => {
        return true
    }
}