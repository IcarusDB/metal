import {Metal} from "./Metal";

export interface Spec {
    version: string,
    metals: Metal[],
    edges: { left: string, right: string }[],
    waitFor: { left: string, right: string }[]
}