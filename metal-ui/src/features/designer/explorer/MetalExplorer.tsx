import {
    Alert,
    Backdrop,
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
import React, { ForwardedRef, forwardRef, useCallback, useEffect, useImperativeHandle, useMemo, useRef, useState } from "react";
import { AiOutlineReload } from "react-icons/ai";
import { VscExpandAll } from "react-icons/vsc";
import { State } from "../../../api/State";
import { useAppSelector } from "../../../app/hooks";
import { metalType, MetalTypes } from "../../../model/Metal";
import { MetalPkg } from "../../../model/MetalPkg";
import { tokenSelector } from "../../user/userSlice";
import { MetalNodeProps, metalViewIcon, MetalViewIcons } from "../MetalView";
import { getAllMetalPkgsOfUserAccess } from "./MetalPkgApi";

const theme = createTheme();

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
        openDetail(metalPkg)
    }

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

export interface MetalPkgDetailProps {
}

export const MetalPkgDetail = forwardRef((props: MetalPkgDetailProps, ref: ForwardedRef<MetalPkgDetailHandler>) => {
    const [isOpen, setOpen] = useState(false);
    const [pkgDetail, setPkg] = useState<MetalPkg | null>(null);

    function open(pkg: MetalPkg) {
        setPkg(pkg);
        setOpen(true);
    }

    function close() {
        setPkg(null)
        setOpen(false);
    }

    useImperativeHandle(
      ref,
      () => ({
        open: open,
        close: close
      }),
      [],
    )

    return (
        <div id="container" style={{position: "relative", margin: "0", height: isOpen? "100%": "0%"}}>
            <Box component={Paper}>
                <Grid container>
                    <Grid item xs={12}>
                        <Button onClick={close}>back</Button>
                    </Grid>
                    <Grid item xs={2}>
                        {"Class"}
                    </Grid>
                    <Grid item xs={10}>
                        <Typography>{pkgDetail === null ? "" : pkgDetail.class}</Typography>
                    </Grid>
                </Grid>
            </Box>
        </div>
        
    );
})

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

    const [status, setStatus] = useState<State>(State.idle);
    const [metalPkgs, setMetalPkgs] = useState<MetalPkg[]>([]);
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
            setStatus(State.pending);
            getAllMetalPkgsOfUserAccess(token).then(
                (pkgs: MetalPkg[]) => {
                    setTimeout(() => {
                        setMetalPkgs(pkgs);
                        setStatus(State.success);
                    }, 3000);
                },
                (reason) => {
                    console.error(reason);
                    setStatus(State.failure);
                }
            );
        }
    }, [token]);

    const openDetail = (pkg: MetalPkg) => {
        if (detailRef.current !== null) {
            detailRef.current.open(pkg)
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
                    <Stack
                        sx={{ width: "auto" }}
                        direction="row"
                        justifyContent="space-between"
                        alignItems="center"
                        divider={<Divider orientation="vertical" flexItem />}
                    >
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
                        <Toolbar sx={{ width: "20%" }}>
                            <IconButton disabled={isPending()} onClick={load}>
                                <AiOutlineReload />
                            </IconButton>
                        </Toolbar>
                    </Stack>
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
                    }}
                >
                    {isFailure() && (
                        <Alert severity={"error"}>{"Fail to load metal packages."}</Alert>
                    )}

                    <List sx={{ overflowY: "scroll", height: "100%" }}>
                        {afterTypeFilter(pkgFilter, metalPkgs).map(
                            (metalPkg: MetalPkg, index: number) => {
                                const props = {
                                    type: metalType(metalPkg.type),
                                    metalPkg: metalPkg,
                                    addNode: addNode,
                                    openDetail: openDetail,
                                };
                                return (
                                    <>
                                        <ListItem key={index} sx={{ width: "auto" }}>
                                            <MetalPkgView {...props} />
                                        </ListItem>
                                    </>
                                );
                            }
                        )}
                    </List>
                    <div style={{position: "relative", margin: "0"}}>
                        <Backdrop open={isPending()} sx={{ position: "absolute" }} />
                    </div>          
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
