import {
    Alert,
    Box,
    Button,
    Container,
    Divider,
    Grid,
    IconButton,
    LinearProgress,
    List,
    ListItem,
    Paper,
    Stack,
    Toolbar,
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
import { VscExpandAll, VscChromeMinimize } from "react-icons/vsc";
import { useAsync } from "../../../api/Hooks";
import { State } from "../../../api/State";
import { useAppSelector } from "../../../app/hooks";
import { metalType, MetalTypes } from "../../../model/Metal";
import { MetalPkg } from "../../../model/MetalPkg";
import { ResizeBackdrop } from "../../ui/ResizeBackdrop";
import { tokenSelector } from "../../user/userSlice";
import { MetalNodeProps, metalViewIcon, MetalViewIcons } from "../MetalView";
import { getAllMetalPkgsOfUserAccess } from "./MetalPkgApi";

const theme = createTheme();
moment.locale("zh_CN");

export interface MetalPkgProps {
    type: MetalTypes;
    metalPkg: MetalPkg;
    addNode: (nodeTmpl: MetalNodeProps) => void;
    openDetail: (pkg: MetalPkg) => void;
}

export function MetalPkgView(props: MetalPkgProps) {
    const { type, metalPkg, addNode, openDetail } = props;
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
        <Box
            sx={{
                flexGrow: 0,
                display: "flex",
                alignItems: "center",
                flexDirection: "row",
                paddingLeft: "0em",
                paddingRight: "0em",
                width: "100%",
            }}
            component={Paper}
        >
            <Container sx={{ width: "30%" }}>
                <h1>{metalViewIcon(type)}</h1>
            </Container>

            <Divider light orientation="vertical" />
            <Stack
                direction="column"
                justifyContent="flex-start"
                alignItems="flex-start"
                spacing={2}
                sx={{ width: "70%" }}
            >
                <Typography
                    sx={{ width: "90%", overflow: "hidden", textOverflow: "ellipsis" }}
                    variant="h6"
                    noWrap={false}
                >
                    {className}
                </Typography>
                <Stack direction="column" justifyContent="flex-start" alignItems="flex-start">
                    <Typography variant="caption">{groupId}</Typography>
                    <Typography variant="caption">{artifactId}</Typography>
                    <Typography variant="caption">{version}</Typography>
                </Stack>
                <Stack
                    direction="row"
                    justifyContent="flex-end"
                    alignItems="flex-end"
                    sx={{ width: "90%" }}
                >
                    {type !== MetalTypes.SETUP && (
                        <Button variant="contained" color="primary" onClick={onAddNode}>
                            {"Add"}
                        </Button>
                    )}
                    <IconButton onClick={onDetail}>
                        <VscExpandAll />
                    </IconButton>
                </Stack>
            </Stack>
        </Box>
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
}

export function MetalExplorer(props: MetalExplorerProps) {
    const { addNode } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const {run, status, result, error} = useAsync<MetalPkg[]>();
    const [pkgFilter, setPkgFilter] = useState<Set<MetalTypes>>(new Set<MetalTypes>());
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
                <Paper sx={{ width: "auto", height: "auto" }}>
                    <div style={{
                        display: "flex",
                        flexDirection: "row",
                        alignContent: "center",
                        justifyContent: "space-between",
                        alignItems: "center",
                    }}>
                        <Toolbar sx={{ width: "80%" }}>
                            <IconButton
                                disabled={isPending()}
                                color={filters.source.isOn() ? "success" : "secondary"}
                                onClick={filters.source.onToggle}
                            >
                                {MetalViewIcons.SOURCE}
                            </IconButton>
                            <IconButton
                                disabled={isPending()}
                                color={filters.sink.isOn() ? "success" : "secondary"}
                                onClick={filters.sink.onToggle}
                            >
                                {MetalViewIcons.SINK}
                            </IconButton>
                            <IconButton
                                disabled={isPending()}
                                color={filters.mapper.isOn() ? "success" : "secondary"}
                                onClick={filters.mapper.onToggle}
                            >
                                {MetalViewIcons.MAPPER}
                            </IconButton>
                            <IconButton
                                disabled={isPending()}
                                color={filters.fusion.isOn() ? "success" : "secondary"}
                                onClick={filters.fusion.onToggle}
                            >
                                {MetalViewIcons.FUSION}
                            </IconButton>
                            <IconButton
                                disabled={isPending()}
                                color={filters.setup.isOn() ? "success" : "secondary"}
                                onClick={filters.setup.onToggle}
                            >
                                {MetalViewIcons.SETUP}
                            </IconButton>
                        </Toolbar>
                        <IconButton disabled={isPending()} onClick={load}>
                                <AiOutlineReload />
                        </IconButton>
                    </div>
                    {progress}
                </Paper>
                <Box
                    component={Paper}
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        height: "90%",
                        width: "auto",
                        overflow: "hidden",
                        position: "relative",
                    }}
                >
                    {isFailure() && (
                        <Alert severity={"error"}>{"Fail to load metal packages."}</Alert>
                    )}

                    <List sx={{ overflowY: "scroll", height: "100%" }}>
                        {afterTypeFilter(pkgFilter, result === null ? [] : result).map(
                            (metalPkg: MetalPkg, index: number) => {
                                const props = {
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
                </Box>
            </div>
        </ThemeProvider>
    );
}
function afterTypeFilter(pkgFilter: Set<MetalTypes>, pkgs: MetalPkg[]) {
    return pkgFilter.size > 0
        ? pkgs.filter((pkg: MetalPkg) => pkgFilter.has(metalType(pkg.type)))
        : pkgs;
}
