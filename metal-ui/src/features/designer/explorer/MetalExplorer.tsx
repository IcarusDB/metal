import {
    Alert,
    AppBar,
    Backdrop,
    Badge,
    Box,
    Button,
    Container,
    Divider,
    IconButton,
    List,
    ListItem,
    Paper,
    Skeleton,
    Stack,
    Toolbar,
    Typography,
} from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import { useCallback, useEffect, useState } from "react";
import { AiOutlineReload } from "react-icons/ai";
import { State } from "../../../api/State";
import { useAppSelector } from "../../../app/hooks";
import { metalType, MetalTypes } from "../../../model/Metal";
import { MetalPkg } from "../../../model/MetalPkg";
import { tokenSelector } from "../../user/userSlice";
import { metalViewIcon, MetalViewIcons } from "../MetalView";
import { getAllMetalPkgsOfUserAccess } from "./MetalPkgApi";

const theme = createTheme();

export interface MetalPkgProps {
    type: MetalTypes;
    metalPkg: MetalPkg;
}

export function MetalPkgView(props: MetalPkgProps) {
    const { type, metalPkg } = props;
    const classSubs = metalPkg.class.split(".");
    const className = classSubs.length > 0 ? classSubs[classSubs.length - 1] : "?";
    const pkgSubs = metalPkg.pkg.split(":");
    const groupId = pkgSubs.length > 0 ? pkgSubs[0] : "?";
    const artifactId = pkgSubs.length > 1 ? pkgSubs[1] : "?";
    const version = pkgSubs.length > 2 ? pkgSubs[2] : "?";

    return (
        <Stack direction="row" justifyContent="stretch" alignItems="center">
            {metalViewIcon(type)}
            <Stack
                direction="column"
                justifyContent="flex-start"
                alignItems="flex-start"
                spacing={2}
            >
                <Typography>{className}</Typography>
                <Stack direction="column" justifyContent="flex-start" alignItems="flex-start">
                    <Typography>{groupId}</Typography>
                    <Typography>{artifactId}</Typography>
                    <Typography>{version}</Typography>
                </Stack>
                <Stack>
                    <Button>{"Add"}</Button>
                </Stack>
            </Stack>
        </Stack>
    );
}

export function MetalExplorer() {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const [status, setStatus] = useState<State>(State.idle);
    const [metalPkgs, setMetalPkgs] = useState<MetalPkg[]>([]);

    const isPending = () => status === State.pending;
    const isFailure = () => status === State.failure;

    const load = useCallback(() => {
        if (token !== null) {
            setStatus(State.pending);
            getAllMetalPkgsOfUserAccess(token).then(
                (pkgs: MetalPkg[]) => {
                    setMetalPkgs(pkgs);
                    setStatus(State.success);
                },
                (reason) => {
                    console.error(reason);
                    setStatus(State.failure);
                }
            );
        }
    }, [token]);

    useEffect(() => {
        load();
    }, [load]);

    if (token === null || metalPkgs === null || metalPkgs.length === 0) {
        return <Skeleton />;
    }

    return (
        <ThemeProvider theme={theme}>
            <AppBar
                color="primary"
                position="sticky"
                sx={{ width: "auto", backgroundColor: "white" }}
            >
                <Stack
                    sx={{ width: "auto" }}
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    divider={<Divider orientation="vertical" flexItem />}
                >
                    <Toolbar sx={{ width: "80%" }}>
                        <IconButton>{MetalViewIcons.SOURCE}</IconButton>
                        <IconButton>{MetalViewIcons.SINK}</IconButton>
                        <IconButton>{MetalViewIcons.MAPPER}</IconButton>
                        <IconButton>{MetalViewIcons.FUSION}</IconButton>
                        <IconButton>{MetalViewIcons.SETUP}</IconButton>
                    </Toolbar>
                    <Toolbar sx={{ width: "20%" }}>
                        <IconButton>
                            <AiOutlineReload />
                        </IconButton>
                    </Toolbar>
                </Stack>
            </AppBar>
            <Backdrop open={isPending()} sx={{ position: "absolute" }} />
            {isFailure() && <Alert severity={"error"}>{"Fail to load projects."}</Alert>}
            <Box
                component={Paper}
                sx={{ display: "flex", height: "100%", width: "100%", overflow: "hidden" }}
            >
                <List sx={{ overflowY: "scroll", width: "inherit" }}>
                    {metalPkgs.map((metalPkg: MetalPkg, index: number) => {
                        const props = {
                            type: metalType(metalPkg.type),
                            metalPkg: metalPkg,
                        };
                        return (
                            <ListItem key={index} sx={{ width: "auto" }}>
                                <MetalPkgView {...props} />
                            </ListItem>
                        );
                    })}
                </List>
            </Box>
        </ThemeProvider>
    );
}
