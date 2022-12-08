import _ from "lodash";
import { useCallback, useEffect } from "react";
import { Connection } from "reactflow";
import { useAsync } from "../../api/Hooks";
import { metalType } from "../../model/Metal";
import { MetalPkg } from "../../model/MetalPkg";
import { Spec } from "../../model/Spec";
import { getAllMetalPkgsOfClasses } from "../../api/MetalPkgApi";
import { MetalNodeProps } from "./MetalView";

export interface SpecLoaderProps {
    spec?: Spec;
}

export interface SpecFlow {
    nodeTmpls: (MetalNodeProps | undefined)[],
    connections: Connection[]
}

export function useSpecLoader(token: string | null, spec?: Spec) {
    const [run, status, result, error] = useAsync<SpecFlow>();

    const loadSpec = useCallback(async () => {
        if (token === null || spec === undefined) {
            return Promise.reject("Fail to load spec.");
        }
        const classes = spec.metals
            .filter((metal) => metal.type !== undefined)
            .map((metal) => (metal.type === undefined ? "" : metal.type));

        if (classes.length === 0) {
            return {
                nodeTmpls: [],
                connections: [],
            };
        }

        return getAllMetalPkgsOfClasses(token, classes).then((metalPkgs) => {
            const nodeTmpls: (MetalNodeProps | undefined)[] = spec.metals
                .filter((metal) => metal.type !== undefined)
                .map((metal) => {
                    const pkg = _.find(metalPkgs, (pkg: MetalPkg) => pkg.class === metal.type);
                    if (pkg === undefined) {
                        return undefined;
                    }
                    const nodeTmpl: MetalNodeProps = {
                        metalPkg: pkg,
                        metal: metal,
                        type: metalType(pkg.type),
                        onUpdate: () => {},
                        onDelete: () => {},
                    };
                    return nodeTmpl;
                });

            const exists = nodeTmpls
                .filter((nodeTmpl) => nodeTmpl !== undefined)
                .map((nodeTmpl) => (nodeTmpl === undefined ? "" : nodeTmpl.metal.id));

            const connects: Connection[] = spec.edges
                .filter((edge) => {
                    const metalId = _.find(exists, (metalId) => edge.left === metalId);
                    return metalId !== undefined;
                })
                .filter((edge) => {
                    const metalId = _.find(exists, (metalId) => edge.right === metalId);
                    return metalId !== undefined;
                })
                .map((edge) => {
                    const connect: Connection = {
                        source: edge.left,
                        target: edge.right,
                        sourceHandle: `${edge.left}-output`,
                        targetHandle: `${edge.right}-input`,
                    };
                    return connect;
                });

            const flow: SpecFlow = {
                nodeTmpls: nodeTmpls,
                connections: connects,
            };
            return flow;
        });
    }, [spec, token]);

    const load = useCallback(()=>{
        run(loadSpec())
    }, [loadSpec, run])

    useEffect(() => {
       load()
    }, [load]);

    return {
        status,
        flow: result,
        error: error
    }
}
