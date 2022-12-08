import {Metal} from "./Metal";

export interface Spec {
    version: string,
    metals: Metal[],
    edges: { left: string, right: string }[],
    waitFor: { left: string, right: string }[]
}

export function emptySpec(): Spec {
    return {
        version: "1.0",
        metals: [],
        edges: [],
        waitFor: [],
    }
}