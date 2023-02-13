import {
    Alert,
    Button,
    Chip,
    Container,
    Divider,
    Grid,
    IconButton,
    LinearProgress,
    List,
    ListItem,
    Paper,
    Typography,
} from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import _ from "lodash";
import moment from "moment";
import React, {
    ForwardedRef,
    forwardRef,
    useCallback,
    useEffect,
    useImperativeHandle,
    useMemo,
    useRef,
    useState,
} from "react";
import { AiOutlineReload } from "react-icons/ai";
import {
    VscExpandAll,
    VscChromeMinimize,
    VscOrganization,
    VscProject,
    VscVersions,
} from "react-icons/vsc";
import { useAsync } from "../../../api/Hooks";
import { State } from "../../../api/State";
import { useAppSelector } from "../../../app/hooks";
import { metalType, MetalTypes } from "../../../model/Metal";
import { MetalPkg } from "../../../model/MetalPkg";
import { ResizeBackdrop } from "../../ui/ResizeBackdrop";
import { tokenSelector } from "../../user/userSlice";
import { MetalNodeProps, metalViewIcon, MetalViewIcons } from "../MetalView";
import { getAllMetalPkgsOfUserAccess } from "../../../api/MetalPkgApi";
import { useFlowPending, useMetalFlow, usePkgs } from "../DesignerProvider";

const theme = createTheme();
moment.locale("zh_CN");

export interface MetalPkgProps {
    isReadOnly?: boolean;
    type: MetalTypes;
    metalPkg: MetalPkg;
    addNode: (nodeTmpl: MetalNodeProps) => void;
    openDetail: (pkg: MetalPkg) => void;
}

export function MetalPkgView(props: MetalPkgProps) {
    const {isReadOnly, type, metalPkg, addNode, openDetail } = props;
    const classSubs = metalPkg.class.split(".");
    const className = classSubs.length > 0 ? classSubs[classSubs.length - 1] : "?";
    const pkgSubs = metalPkg.pkg.split(":");
    const groupId = pkgSubs.length > 0 ? pkgSubs[0] : "?";
    const artifactId = pkgSubs.length > 1 ? pkgSubs[1] : "?";
    const version = pkgSubs.length > 2 ? pkgSubs[2] : "?";

    const onAddNode = () => {
        const nodeTmpl: MetalNodeProps = {
            type: type,
            onDelete: () => {},
            onUpdate: () => {},
            inputs: () => ([]),
            outputs: () => ([]),
            metal: {
                id: "node-0",
                name: "node-0",
                props: {},
            },
            metalPkg: metalPkg,
        };
        addNode(nodeTmpl);
    };

    const onDetail = () => {
        openDetail(metalPkg);
    };

    return (
        <Paper
            variant="outlined"
            square
            sx={{
                flexGrow: 0,
                display: "flex",
                alignItems: "center",
                flexDirection: "row",
                paddingLeft: "0em",
                paddingRight: "0em",
                width: "100%",
            }}
        >
            <Grid
                container
                sx={{
                    paddingLeft: "1em",
                    paddingTop: "1em",
                }}
            >
                <Grid
                    item
                    xs={3}
                    sx={{
                        display: "flex",
                        alignItems: "flex-start",
                        justifyContent: "flex-start",
                    }}
                >
                    <div
                        style={{
                            fontSize: "2em",
                        }}
                    >
                        {metalViewIcon(type)}
                    </div>
                </Grid>
                <Grid item xs={9}>
                    <Typography
                        sx={{ width: "100%", overflow: "hidden", textOverflow: "ellipsis" }}
                        variant="h6"
                        noWrap={false}
                    >
                        {className}
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
                    <VscOrganization />
                </Grid>
                <Grid item xs={11}>
                    <Chip label={groupId} size="small" color="primary" variant="outlined" />
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
                    <VscProject />
                </Grid>
                <Grid item xs={11}>
                    <Typography variant="caption">{artifactId}</Typography>
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
                    <VscVersions />
                </Grid>
                <Grid item xs={11}>
                    <Chip size="small" variant="outlined" label={version} color={"info"} />
                </Grid>
                <Grid item xs={12}>
                    <Divider
                        orientation="horizontal"
                        flexItem
                        sx={{
                            paddingTop: "1vh",
                        }}
                    />
                </Grid>
                <Grid item xs={6}></Grid>
                <Grid item xs={4}>
                    {type !== MetalTypes.SETUP && (
                        <Button
                            disabled={isReadOnly}
                            variant="contained"
                            color="primary"
                            onClick={onAddNode}
                            style={{
                                width: "100%",
                            }}
                        >
                            {"Add"}
                        </Button>
                    )}
                </Grid>
                <Grid item xs={2}>
                    <IconButton onClick={onDetail}>
                        <VscExpandAll />
                    </IconButton>
                </Grid>
            </Grid>
        </Paper>
    );
}

export interface MetalPkgDetailHandler {
    open: (pkg: MetalPkg) => void;
    close: () => void;
}

export interface MetalPkgDetailProps {}

export const MetalPkgDetail = forwardRef(
    (props: MetalPkgDetailProps, ref: ForwardedRef<MetalPkgDetailHandler>) => {
        const [isOpen, setOpen] = useState(false);
        const [pkgDetail, setPkg] = useState<MetalPkg | null>(null);

        function open(pkg: MetalPkg) {
            setPkg(pkg);
            setOpen(true);
        }

        function close() {
            setPkg(null);
            setOpen(false);
        }

        useImperativeHandle(
            ref,
            () => ({
                open: open,
                close: close,
            }),
            []
        );

        return (
            <Paper
                square
                elevation={3}
                sx={{
                    position: "relative",
                    width: "100%",
                    height: "100%",
                    display: isOpen ? "block" : "none",
                    wordBreak: "break-all",
                }}
            >
                <div
                    style={{
                        height: "100%",
                        width: "100%",
                        display: "flex",
                        flexDirection: "column",
                        alignContent: "space-between",
                        justifyContent: "flex-start",
                    }}
                >
                    <Paper
                        variant="outlined"
                        square
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignContent: "space-between",
                            justifyContent: "flex-end",
                            // backgroundColor: "silver",
                        }}
                    >
                        <IconButton onClick={close}>
                            <VscChromeMinimize />
                        </IconButton>
                    </Paper>
                    <Container>
                        <Grid container>
                            <Grid item xs={4}>
                                Type
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>{pkgDetail === null ? "" : pkgDetail.type}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Scope
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>{pkgDetail === null ? "" : pkgDetail.scope}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Class
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>{pkgDetail === null ? "" : pkgDetail.class}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Group ID
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>
                                    {pkgDetail === null ? "" : pkgDetail.groupId}
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Artifact ID
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>
                                    {pkgDetail === null ? "" : pkgDetail.artifactId}
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Publisher
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>
                                    {pkgDetail === null ? "" : pkgDetail.userId}
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                Publish Time
                            </Grid>
                            <Grid item xs={8}>
                                <Typography>
                                    {pkgDetail === null
                                        ? ""
                                        : moment(pkgDetail.createTime).format(
                                              "YYYY-MM-DD HH:mm:ss"
                                          )}
                                </Typography>
                            </Grid>
                        </Grid>
                    </Container>
                </div>
            </Paper>
        );
    }
);

interface ITypeFilter {
    isOn: () => boolean;
    onToggle: () => void;
}

function TypeFilter(
    type: MetalTypes,
    pkgFilter: Set<MetalTypes>,
    setPkgFilter: React.Dispatch<React.SetStateAction<Set<MetalTypes>>>
): ITypeFilter {
    return {
        isOn: () => pkgFilter.has(type),
        onToggle: () => {
            if (pkgFilter.has(type)) {
                pkgFilter.delete(type);
            } else {
                pkgFilter.add(type);
            }
            setPkgFilter(_.clone(pkgFilter));
        },
    };
}

interface MetalExplorerProps {
    addNode: (nodeTmpl: MetalNodeProps) => void;
    restrictPkgs?: string[];
}

export function MetalExplorer(props: MetalExplorerProps) {
    const { addNode, restrictPkgs } = props;
    const [isFlowPending] = useFlowPending();
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [run, status, result] = useAsync<MetalPkg[]>();
    const [pkgFilter, setPkgFilter] = useState<Set<MetalTypes>>(new Set<MetalTypes>());

    const pkgs =
        result === null
            ? []
            : restrictPkgs === undefined
            ? result
            : result.filter(
                  (pkg) =>
                      _.find(restrictPkgs, (restrictPkg) => restrictPkg === pkg.pkg) !== undefined
              );
    const detailRef = useRef<MetalPkgDetailHandler>(null);

    const isPending = () => status === State.pending;
    const isFailure = () => status === State.failure;

    const filters = useMemo(
        () => ({
            source: TypeFilter(MetalTypes.SOURCE, pkgFilter, setPkgFilter),
            sink: TypeFilter(MetalTypes.SINK, pkgFilter, setPkgFilter),
            mapper: TypeFilter(MetalTypes.MAPPER, pkgFilter, setPkgFilter),
            fusion: TypeFilter(MetalTypes.FUSION, pkgFilter, setPkgFilter),
            setup: TypeFilter(MetalTypes.SETUP, pkgFilter, setPkgFilter),
        }),
        [pkgFilter, setPkgFilter]
    );

    const load = useCallback(() => {
        if (token !== null) {
            run(getAllMetalPkgsOfUserAccess(token));
        }
    }, [run, token]);

    const openDetail = (pkg: MetalPkg) => {
        if (detailRef.current !== null) {
            detailRef.current.open(pkg);
        }
    };

    useEffect(() => {
        load();
    }, [load]);

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    return (
        <ThemeProvider theme={theme}>
            <div
                style={{
                    height: "100%",
                    width: "auto",
                    display: "flex",
                    flexDirection: "column",
                    alignContent: "space-between",
                    justifyContent: "space-between",
                }}
            >
                <Paper
                    square
                    variant="outlined"
                    sx={{
                        boxSizing: "border-box",
                        display: "flex",
                        flexDirection: "row",
                        justifyContent: "space-between",
                        alignItems: "center",
                        height: "6%",
                        // backgroundColor: "#d8c3c366",
                    }}
                >
                    <div
                        style={{
                            boxSizing: "border-box",
                            padding: "0.5em",
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <IconButton
                            disabled={isPending()}
                            color={filters.source.isOn() ? "success" : "secondary"}
                            onClick={filters.source.onToggle}
                            sx={{
                                borderRadius: "0px",
                            }}
                            size="small"
                        >
                            {MetalViewIcons.SOURCE}
                        </IconButton>
                        <IconButton
                            disabled={isPending()}
                            color={filters.sink.isOn() ? "success" : "secondary"}
                            onClick={filters.sink.onToggle}
                            sx={{
                                borderRadius: "0px",
                            }}
                            size="small"
                        >
                            {MetalViewIcons.SINK}
                        </IconButton>
                        <IconButton
                            disabled={isPending()}
                            color={filters.mapper.isOn() ? "success" : "secondary"}
                            onClick={filters.mapper.onToggle}
                            sx={{
                                borderRadius: "0px",
                            }}
                            size="small"
                        >
                            {MetalViewIcons.MAPPER}
                        </IconButton>
                        <IconButton
                            disabled={isPending()}
                            color={filters.fusion.isOn() ? "success" : "secondary"}
                            onClick={filters.fusion.onToggle}
                            sx={{
                                borderRadius: "0px",
                            }}
                            size="small"
                        >
                            {MetalViewIcons.FUSION}
                        </IconButton>
                        <IconButton
                            disabled={isPending()}
                            color={filters.setup.isOn() ? "success" : "secondary"}
                            onClick={filters.setup.onToggle}
                            sx={{
                                borderRadius: "0px",
                            }}
                            size="small"
                        >
                            {MetalViewIcons.SETUP}
                        </IconButton>
                    </div>
                    <IconButton
                        disabled={isPending()}
                        onClick={load}
                        sx={{
                            borderRadius: "0px",
                        }}
                    >
                        <AiOutlineReload />
                    </IconButton>
                </Paper>
                {progress}
                <Paper
                    square
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        height: "92%",
                        width: "auto",
                        overflow: "hidden",
                        position: "relative",
                    }}
                >
                    {isFailure() && (
                        <Alert severity={"error"}>{"Fail to load metal packages."}</Alert>
                    )}

                    <List sx={{ overflowY: "scroll", height: "100%" }}>
                        {afterTypeFilter(pkgFilter, pkgs).map(
                            (metalPkg: MetalPkg, index: number) => {
                                const props = {
                                    isReadOnly: isFlowPending,
                                    type: metalType(metalPkg.type),
                                    metalPkg: metalPkg,
                                    addNode: addNode,
                                    openDetail: openDetail,
                                };
                                return (
                                    <ListItem key={index} sx={{ width: "auto" }}>
                                        <MetalPkgView {...props} />
                                    </ListItem>
                                );
                            }
                        )}
                    </List>
                    <ResizeBackdrop open={isPending()} />
                    <MetalPkgDetail ref={detailRef}></MetalPkgDetail>
                </Paper>
            </div>
        </ThemeProvider>
    );
}
function afterTypeFilter(pkgFilter: Set<MetalTypes>, pkgs: MetalPkg[]) {
    return pkgFilter.size > 0
        ? pkgs.filter((pkg: MetalPkg) => pkgFilter.has(metalType(pkg.type)))
        : pkgs;
}

export function MetalExplorerWrapper() {
    const [pkgs] = usePkgs();
    const [metalFlowAction] = useMetalFlow();
    const onAddNode = useCallback(
        (nodeProps: MetalNodeProps) => {
            metalFlowAction.addNode(nodeProps);
        },
        [metalFlowAction]
    );

    return (
        <MetalExplorer addNode={onAddNode} restrictPkgs={pkgs} />
    )
}
