import { ImUpload, ImDownload } from "react-icons/im";
import { AiOutlineFunction, AiOutlineDelete } from "react-icons/ai";
import { VscMerge, VscExpandAll, VscExtensions, VscSymbolClass, VscRunErrors, VscDebug } from "react-icons/vsc";
import { Connection, Node, Edge, NodeProps } from "reactflow";
import { Handle, Position } from "reactflow";
import {
    Button,
    Paper,
    Badge,
    Divider,
    Typography,
    Grid,
    IconButton,
} from "@mui/material";
import { MouseEvent, useCallback, useMemo, useState } from "react";
import { MetalPkg } from "../../model/MetalPkg";
import { Metal, Metals, MetalTypes } from "../../model/Metal";
import { GraphTopology } from "../../model/GraphTopology";
import { HotNode, MetalNodeEditorAction } from "./DesignerActionSlice";
import { IReadOnly } from "../ui/Commons";
import { ScaleLoader } from "react-spinners";
import { MdInput, MdOutlineCheckCircle, MdRadioButtonChecked, MdRadioButtonUnchecked } from "react-icons/md";
import { GrDocumentConfig, GrTip } from "react-icons/gr";
import _ from "lodash";
import { ProblemsNotice } from "./backend/BackendBar";
import { BackendState, BackendStatus } from "../../model/Project";
import { useBackendStatusFn, useFlowPendingFn, useHotNodesFn, useMessagsLogger, useMetalFlowFn, useModifyFn, useProjectIdFn } from "./DesignerProvider";
import { tokenSelector } from "../user/userSlice";
import { useAppSelector } from "../../app/hooks";
import { useDesignerAsync } from "./DesignerHooks";
import { AnalysisResponse, analysisSubSpecOfId } from "../../api/ProjectApi";
import { State } from "../../api/State";
import { Spec } from "../../model/Spec";
import { ApiResponse } from "../../api/APIs";

export const MetalViewIcons = {
    SOURCE: <ImUpload />,
    SINK: <ImDownload />,
    MAPPER: <AiOutlineFunction />,
    FUSION: <VscMerge />,
    SETUP: <VscExtensions />,
};

export function metalViewIcon(type: MetalTypes) {
    switch (type) {
        case MetalTypes.SOURCE:
            return MetalViewIcons.SOURCE;
        case MetalTypes.SINK:
            return MetalViewIcons.SINK;
        case MetalTypes.MAPPER:
            return MetalViewIcons.MAPPER;
        case MetalTypes.FUSION:
            return MetalViewIcons.FUSION;
        default:
            return MetalViewIcons.SETUP;
    }
}

export enum MetalNodeState {
    UNANALYSIS = "UNANALYSIS",
    ANALYSISED = "ANALYSISED",
    EXECED = "EXECED",
    PENDING = "PENDING",
    ERROR = "ERROR",
}

export interface MetalNodeProps extends IReadOnly {
    metalPkg: MetalPkg;
    metal: Metal;
    type: MetalTypes;
    onUpdate: (newMetal: Metal) => void;
    onDelete: () => void;
    inputs: (id: string) => Node<MetalNodeProps>[];
    outputs: (id: string) => Node<MetalNodeProps>[];
    backendStatus: () => BackendStatus | undefined;
    editor?: MetalNodeEditorAction;
    status?: MetalNodeState;
    msg?: string;
}

export interface IMetalNodeView {
    inputHandle: (props: MetalNodeProps) => JSX.Element;
    outputHandle: (props: MetalNodeProps) => JSX.Element;
    logo: (props: MetalNodeProps) => JSX.Element;
}

const inputHandle = (props: MetalNodeProps) => {
    return <Handle type={"target"} id={`${props.metal.id}-input`} position={Position.Top}></Handle>;
};

const outputHandle = (props: MetalNodeProps) => {
    return (
        <Handle type={"source"} id={`${props.metal.id}-output`} position={Position.Bottom}></Handle>
    );
};

export const MetalSourceNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return <></>;
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SOURCE;
    },
};

export const MetalSinkNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return <></>;
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.SINK;
    },
};

export const MetalMapperNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.MAPPER;
    },
};

export const MetalFusionNodeView: IMetalNodeView = {
    inputHandle: (props: MetalNodeProps) => {
        return inputHandle(props);
    },
    outputHandle: (props: MetalNodeProps) => {
        return outputHandle(props);
    },
    logo: (props: MetalNodeProps) => {
        return MetalViewIcons.FUSION;
    },
};

export const MetalNodeViews = {
    metalNodeView: (type: string) => {
        switch (type) {
            case MetalTypes.SOURCE:
                return MetalSourceNodeView;
            case MetalTypes.SINK:
                return MetalSinkNodeView;
            case MetalTypes.FUSION:
                return MetalFusionNodeView;
            case MetalTypes.MAPPER:
                return MetalMapperNodeView;
            default:
                return MetalSourceNodeView;
        }
    },
};

export interface MetalNodeStateTipProps {
    status: MetalNodeState
}

function metalNodeStateView(status: MetalNodeState) {
    switch (status) {
        case MetalNodeState.UNANALYSIS:
            return (
                <MdRadioButtonUnchecked />
            );
        case MetalNodeState.ANALYSISED:
            return (
                <MdRadioButtonChecked />
            );
        case MetalNodeState.EXECED:
            return (
                <MdOutlineCheckCircle />
            );
        case MetalNodeState.PENDING:
            return (
                <ScaleLoader color="#36d7b7" />
            );
        case MetalNodeState.ERROR:
            return (
                <VscRunErrors />
            );
    }
}

export function MetalNodeStateTip(props: MetalNodeStateTipProps) {
    const {status} = props;
    const color = status === MetalNodeState.ERROR? "error": "info";
    return (
        <Paper sx={{
            width: "4em",
            height: "4em",
            display: "flex",
            alignContent: "center",
            justifyContent: "center",
            alignItems: "center"
        }}
            square
            variant="outlined"
        >
            <IconButton 
                color={color}
            >
                {metalNodeStateView(status)}
            </IconButton>
        </Paper>
        
    )
};

export function onConnectValid(
    connection: Connection,
    nodes: Node<MetalNodeProps>[],
    edges: Edge<any>[]
) {
    if (connection.target === null || connection.source === null) {
        return false;
    }

    if (connection.target === connection.source) {
        return false;
    }

    const nodesOnGraph = new Set<string>(nodes.map((node) => node.id));
    const edgesOnGraph = edges.map((edge) => {
        return { source: edge.source, target: edge.target };
    });

    const graphTopology: GraphTopology = new GraphTopology(nodesOnGraph, edgesOnGraph);
    let isValid = true;
    const inputs: Node<MetalNodeProps>[] = nodes.filter((node) => node.id === connection.target);
    inputs.forEach((node) => {
        isValid =
            isValid &&
            Metals.metal(node.data.type).hasInput() &&
            Metals.metal(node.data.type).canAddInput(graphTopology, node.id);
    });
    if (!isValid) {
        return false;
    }

    const outputs: Node<MetalNodeProps>[] = nodes.filter((node) => node.id === connection.source);
    outputs.forEach((node) => {
        isValid = isValid && Metals.metal(node.data.type).hasOutput();
    });
    if (!isValid) {
        return false;
    }

    const next = graphTopology.next(connection.target);
    return !next.has(connection.source);
}

export function MetalNode(props: NodeProps<MetalNodeProps>) {
    const {id} = props;
    const { metal, metalPkg, type, onDelete, onUpdate, inputs, outputs} = props.data;
    const status = props.data.status === undefined? MetalNodeState.UNANALYSIS: props.data.status;
    const msg = props.data.msg;
    const isReadOnly = props.data.isReadOnly || status === MetalNodeState.PENDING;
    const editor = props.data.editor;
    const nodeView: IMetalNodeView = MetalNodeViews.metalNodeView(type);

    const onEdit = useCallback((event: MouseEvent<HTMLAnchorElement> | MouseEvent<HTMLButtonElement>) => {
        if (editor === undefined) {
            return;
        }
        editor.load(props.data);
    }, [editor, props.data]);


    const badgeContent = useMemo(() => (
        <MetalNodeStateTip status={status} />
    ), [status]);

    const isPropDefined = ()=> (!_.isEmpty(metal.props));

    const isInputReady = useCallback(() => {
        if (type === MetalTypes.SOURCE || type === MetalTypes.SETUP) {
            return true;
        }
        const inputNds = inputs(id).length;
        if (inputNds === 0) {
            return false;
        }
        if ((type === MetalTypes.MAPPER || type === MetalTypes.SINK) && inputNds === 1) {
            return true;
        }
        if (type === MetalTypes.FUSION && inputNds > 1) {
            return true;
        }
        return false;
    }, [id, inputs, type]);
    
    const view = useMemo(
        () => (
            <Badge color="default" badgeContent={badgeContent}>
                <div>
                    {nodeView.inputHandle(props.data)}
                    <Paper
                        square
                        variant="outlined"
                        sx={{
                            backgroundColor: "#66ffcc",
                            borderWidth: "5px",
                        }}
                    >
                        <div
                            style={{
                                display: "flex",
                                boxSizing: "border-box",
                                width: "100%",
                                flexDirection: "row",
                                flexWrap: "wrap",
                                alignContent: "center",
                                justifyContent: "space-around",
                                alignItems: "center",
                                wordBreak: "break-all",
                                paddingTop: "1vh",
                                paddingBottom: "1vh",
                                paddingRight: "1vw",
                                paddingLeft: "1vw",
                            }}
                        >
                            <div
                                style={{
                                    display: "flex",
                                    width: "100%",
                                    flexDirection: "column",
                                    flexWrap: "wrap",
                                    alignContent: "flex-start",
                                    justifyContent: "flex-start",
                                    alignItems: "flex-start",
                                }}
                            >
                                <div
                                    style={{
                                        display: "flex",
                                        width: "100%",
                                        flexDirection: "row",
                                        flexWrap: "nowrap",
                                        alignContent: "center",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                    }}
                                >
                                    <div
                                        style={{
                                            display: "flex",
                                            width: "100%",
                                            flexDirection: "row",
                                            flexWrap: "wrap",
                                            alignContent: "center",
                                            justifyContent: "flex-start",
                                            alignItems: "center",
                                        }}
                                    >
                                        <div
                                            style={{
                                                fontSize: "2em",
                                                padding: "1vh",
                                            }}
                                        >
                                            {nodeView.logo(props.data)}
                                        </div>
                                        <Divider orientation="vertical" flexItem />
                                        <Typography
                                            variant={"h6"}
                                            sx={{
                                                paddingLeft: "1vw",
                                            }}
                                        >
                                            {metal.name}
                                        </Typography>
                                    </div>
                                    <div
                                        style={{
                                            display: "flex",
                                            flexDirection: "column",
                                            flexWrap: "wrap",
                                            alignContent: "center",
                                            justifyContent: "flex-end",
                                            alignItems: "center",
                                        }}
                                    >
                                        <Button
                                            size="small"
                                            sx={{ borderTopLeftRadius: 0 }}
                                            onClick={onEdit}
                                        >
                                            <VscExpandAll />
                                        </Button>
                                        {!isReadOnly && (
                                            <Button
                                                size="small"
                                                sx={{ borderBottomLeftRadius: 0 }}
                                                onClick={onDelete}
                                                disabled={isReadOnly}
                                            >
                                                <AiOutlineDelete />
                                            </Button>
                                        )}
                                        {!isReadOnly && isInputReady() && isPropDefined() && (
                                            <NodeAnalysis
                                                id={metal.id}
                                                content={"Analysis"}
                                                isContainNode={true}
                                                isReadOnly={isReadOnly}
                                            />
                                        )}
                                    </div>
                                </div>

                                <Divider orientation="horizontal" flexItem />
                                <Grid container>
                                    <Grid
                                        item
                                        xs={1}
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                        }}
                                    >
                                        <VscSymbolClass fontSize={"1em"} />
                                    </Grid>
                                    <Grid item xs={11}>
                                        <Typography variant={"caption"} color={"GrayText"}>
                                            {metalPkg.class}
                                        </Typography>
                                    </Grid>
                                </Grid>
                                <Divider orientation="horizontal" flexItem />
                                <Grid
                                    container
                                    sx={{
                                        backgroundColor: "white",
                                    }}
                                >
                                    <Grid
                                        item
                                        xs={1}
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                        }}
                                    >
                                        <GrDocumentConfig fontSize={"1em"} />
                                    </Grid>
                                    <Grid item xs={11}>
                                        <Typography variant={"caption"} color={"GrayText"}>
                                            {isPropDefined() ? "Defined" : "Undefined"}
                                        </Typography>
                                    </Grid>
                                    <Grid
                                        item
                                        xs={1}
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                        }}
                                    >
                                        <MdInput fontSize={"1em"} />
                                    </Grid>
                                    <Grid item xs={11}>
                                        <Typography variant={"caption"} color={"GrayText"}>
                                            {isInputReady() ? "Ready" : "Unready"}
                                        </Typography>
                                        {isReadOnly || !isInputReady() ? (
                                            ""
                                        ) : (
                                            <NodeAnalysis
                                                id={metal.id}
                                                content={"Analysis all inputs"}
                                                isContainNode={false}
                                                isReadOnly={isReadOnly}
                                            />
                                        )}
                                    </Grid>
                                    <Grid
                                        item
                                        xs={1}
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                        }}
                                    >
                                        <GrTip fontSize={"1em"} />
                                    </Grid>
                                    <Grid item xs={11}>
                                        <Typography variant={"caption"} color={"GrayText"}>
                                            {status}
                                        </Typography>
                                        {status === MetalNodeState.ERROR && msg && (
                                            <ProblemsNotice problem={msg} />
                                        )}
                                    </Grid>
                                </Grid>
                            </div>
                        </div>
                    </Paper>
                    {nodeView.outputHandle(props.data)}
                </div>
            </Badge>
        ),
        [badgeContent, isInputReady, isPropDefined, isReadOnly, metal.id, metal.name, metalPkg.class, msg, nodeView, onDelete, onEdit, props.data, status]
    );
    return (<>{view}</>)

    
}

export const MetalNodeTypes = {
    metal: MetalNode,
};


function useNodeAnalysis(token: string | null, id: string | undefined, scope: ()=> string[]): [
    (spec: Spec, subSpec: Spec) => void,
    State
] {
    const [,setFlowPending] = useFlowPendingFn();
    const [,setHotNodes] = useHotNodesFn();
    const [,modify] = useModifyFn();
    const [run, status] = useDesignerAsync<AnalysisResponse>({
        onSuccess: (result) => {
            const analysed = result.analysed.map(ide => {
                const r: HotNode= [ide, MetalNodeState.ANALYSISED, undefined];
                return r;
            });
            const unAnalysed = result.unAnalysed.map(ide => {
                const r: HotNode = [ide, MetalNodeState.UNANALYSIS, undefined];
                return r;
            });
           setFlowPending(false);
           modify(false);
            setHotNodes([
                ...analysed,
                ...unAnalysed,
            ])
        },
        onPending: () => {
            setFlowPending(true);
            setHotNodes(
                scope().map(nd => [nd, MetalNodeState.PENDING, undefined])
            );
        },
        onError: (reason) => {
            setFlowPending(false);
            modify(true);
            const errorMsg = ApiResponse.extractErrorMessage(reason);
            if (errorMsg) {
                const metalIds = ApiResponse.extractMetalIds(errorMsg);
                if (metalIds) {
                    setHotNodes(
                        scope().map(nd => {
                            if (_.find(metalIds, (mid => mid === nd))) {
                                return [nd, MetalNodeState.ERROR, errorMsg];
                            }
                            return [nd, MetalNodeState.UNANALYSIS, undefined];
                        })
                    );
                } else {
                    setHotNodes(
                        scope().map(nd => [nd, MetalNodeState.ERROR, errorMsg])
                    );
                }
            } else {
                setHotNodes(
                    scope().map(nd => [nd, MetalNodeState.ERROR, "Fail to analysis."])
                );
            }
            
        }
    });
    const analysis = useCallback((spec: Spec, subSpec: Spec) => {
        if (token === null || id === undefined) {
            return;
        }
        run(analysisSubSpecOfId(token, id, spec, subSpec));
    }, [id, run, token]);
    
    return [analysis, status];
}

interface NodeAnalysisProps extends IReadOnly {
    id: string,
    isContainNode: boolean,
    content: string,
}

function NodeAnalysis(props: NodeAnalysisProps) {
    const {id, isReadOnly, isContainNode, content} = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [getProjectId] = useProjectIdFn();
    const [flowAction] = useMetalFlowFn();
    const {warning} = useMessagsLogger();
    const scope = useCallback(() => {
        if (flowAction) {
            const action = flowAction();
            if (action) {
                const spec = action.exportSubSpec(id, isContainNode);
                if (spec) {
                    return spec.metals.map(m => m.id);
                }
            }
        }
        return [];
    }, [flowAction, id, isContainNode]);
    const [getBackendStatus] = useBackendStatusFn();
    const [analysis, analysisStatus] = useNodeAnalysis(token, getProjectId(), scope);
    const [isTipFail , setTipFail] = useState<boolean>(false);
    const isPending = analysisStatus === State.pending;
    const isFail = analysisStatus === State.failure || isTipFail;

    const onClick = () => {
        const action = flowAction();
        if (action) {
            const projectId = getProjectId();
            if (projectId === undefined) {
                warning("Project id is undefined.")
                setTipFail(true);
                return;
            }
            const backendStatus = getBackendStatus();
            if (backendStatus === undefined || backendStatus.current !== BackendState.UP) {
                warning("Backend is not up.");
                return;
            }

            const spec = action.export();
            const subSpec = action.exportSubSpec(id, isContainNode);
            if (subSpec === undefined) {
                warning("Fail export subSpec.");
                setTipFail(true);
                return;
            }
            const checkResult = action.checkSpec(subSpec);
            if (checkResult.emptyMetals.length !== 0 || checkResult.inputsIllegalMetals.length !== 0) {
                const emptyMsg = `Property undefined: [${checkResult.emptyMetals.join(",")}]`;
                const illegalInputsMsg = `Illegal inputs: [${checkResult.inputsIllegalMetals.map(m => m.metal).join(",")}]`
                const warnMsg = `${emptyMsg}\n${illegalInputsMsg}`;
                warning(warnMsg)
                setTipFail(true);
                return;
            }
            analysis(spec, subSpec);
        }
    }

    return (
        <IconButton
            color={isFail? "error": "info"}
            onClick={onClick}
            size={"small"}
            disabled={isReadOnly || isPending}
        >
            <VscDebug />
        </IconButton>
    );
}
