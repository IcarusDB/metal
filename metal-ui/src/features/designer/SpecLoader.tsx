import _ from "lodash";
import { useCallback, useEffect } from "react";
import { Connection } from "reactflow";
import { useAsync } from "../../api/Hooks";
import { metalType } from "../../model/Metal";
import { MetalPkg } from "../../model/MetalPkg";
import { Spec } from "../../model/Spec";
import { getAllMetalPkgsOfClasses } from "../../api/MetalPkgApi";
import { MetalNodeProps, MetalNodeState } from "./MetalView";
import { useSpec, useSpecFlow, useSpecFlowFn } from "./DesignerProvider";
import { State } from "../../api/State";
import { Alert } from "@mui/material";



export interface SpecFlow {
    nodeTmpls: (MetalNodeProps | undefined)[],
    connections: Connection[]
}

export function useSpecLoader(token: string | null): [()=>void, State, SpecFlow | null, any] {
    const [spec] = useSpec();
    const [run, status, result, error] = useAsync<SpecFlow>();
    const [setSpecFlow] = useSpecFlowFn();

    const load = useCallback(() => {
        if (token === null || spec === undefined) {
            return;
        }
        const classes = spec.metals
            .filter((metal) => metal.type !== undefined)
            .map((metal) => (metal.type === undefined ? "" : metal.type));

        if (classes.length === 0) {
            const flow: SpecFlow = {
                nodeTmpls: [],
                connections: []
            }
            setSpecFlow(flow);
            return;
        }

        const task = getAllMetalPkgsOfClasses(token, classes)
        .then((metalPkgs) => {
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
                        inputs: (id: string) => ([]),
                        outputs: (id: string) => ([]),
                        // status: MetalNodeState.PENDING,
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
            setSpecFlow(flow);
            return flow;
        });
        run(task);
    }, [run, setSpecFlow, spec, token]);

    // useEffect(() => {
    //    load()
    // }, [load]);

    return [load, status, result, error];
}


export interface SpecLoaderProps {
    token: string | null;
}
export function SpecLoader(props: SpecLoaderProps) {
    const {token} = props;
    const [load, status,] = useSpecLoader(token);
    
    useEffect(()=>{
        load();
    }, [load])

    return (
        <>
            {status === State.failure && (
                <Alert severity={"error"}>{"Fail to load project spec."}</Alert>
            )}
        </>
    )
}
