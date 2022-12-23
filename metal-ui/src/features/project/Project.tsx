import "./Project.css";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import {
    Paper,
    Divider,
    IconButton,
    Tooltip,
    CircularProgress,
    Alert,
    LinearProgress,
    Grid,
} from "@mui/material";
import Stack from "@mui/material/Stack";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import {
    AiOutlineStop,
    AiOutlineApi,
    AiFillThunderbolt,
    AiOutlineWarning,
    AiOutlineQuestionCircle,
    AiOutlineEye,
    AiOutlineEdit,
    AiOutlineReload,
} from "react-icons/ai";
import { HiStop } from "react-icons/hi";
import { useCallback, useEffect, useMemo } from "react";
import { BackendState, BackendStatus, Deploy, Project } from "../../model/Project";
import { getAllProjectOfUser } from "../../api/ProjectApi";
import { State } from "../../api/State";
import { useAsync } from "../../api/Hooks";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { MainHandler } from "../main/Main";
import { DataGrid, GridColDef, GridRenderCellParams, GridToolbarContainer } from "@mui/x-data-grid";
import moment from "moment";

function backendStatusTip(backendStatus: BackendStatus) {
    switch (backendStatus.current) {
        case BackendState.CREATED:
            return (
                <Grid container>
                    <Grid item xs={4}>
                        CURRENT
                    </Grid>
                    <Grid item xs={8}>
                        {backendStatus.current}
                    </Grid>
                    <Grid item xs={4}>
                        Created Time
                    </Grid>
                    <Grid item xs={8}>
                        {moment(backendStatus.createdTime).format("YYYY-MM-DD HH:mm:ss")}
                    </Grid>
                </Grid>
            );
        case BackendState.UP:
            return (
                <Grid container>
                    <Grid item xs={4}>
                        CURRENT
                    </Grid>
                    <Grid item xs={8}>
                        {backendStatus.current}
                    </Grid>
                    <Grid item xs={4}>
                        Up Time
                    </Grid>
                    <Grid item xs={8}>
                        {moment(backendStatus.upTime).format("YYYY-MM-DD HH:mm:ss")}
                    </Grid>
                </Grid>
            );
        case BackendState.DOWN:
            return (
                <Grid container>
                    <Grid item xs={4}>
                        CURRENT
                    </Grid>
                    <Grid item xs={8}>
                        {backendStatus.current}
                    </Grid>
                    <Grid item xs={4}>
                        Down Time
                    </Grid>
                    <Grid item xs={8}>
                        {moment(backendStatus.downTime).format("YYYY-MM-DD HH:mm:ss")}
                    </Grid>
                </Grid>
            );
        case BackendState.FAILURE:
            return (
                <Grid container>
                    <Grid item xs={4}>
                        CURRENT
                    </Grid>
                    <Grid item xs={8}>
                        {backendStatus.current}
                    </Grid>
                    <Grid item xs={4}>
                        Failure Time
                    </Grid>
                    <Grid item xs={8}>
                        {moment(backendStatus.failureTime).format("YYYY-MM-DD HH:mm:ss")}
                    </Grid>
                    <Grid item xs={4}>
                        Message
                    </Grid>
                    <Grid item xs={8}>
                        {backendStatus.failureMsg}
                    </Grid>
                </Grid>
            );
    }
}

function backendStatus(deploy: Deploy | undefined) {
    if (deploy === undefined) {
        return "?";
    }
    if (deploy.backend === undefined) {
        return (
            <Tooltip title={"No deployment is set."}>
                <IconButton>
                    <AiOutlineStop />
                </IconButton>
            </Tooltip>
        );
    }

    switch (deploy.backend.status.current) {
        case BackendState.CREATED:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton color="primary">
                        <AiOutlineApi />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.UP:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton color="success">
                        <AiFillThunderbolt />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.DOWN:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton color="info">
                        <HiStop />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.FAILURE:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton color="error">
                        <AiOutlineWarning />
                    </IconButton>
                </Tooltip>
            );
        default:
            return (
                <Tooltip title={"Unknown"}>
                    <AiOutlineQuestionCircle />
                </Tooltip>
            );
    }
}

const theme = createTheme();

export interface ProjectListProps {
    mainHandler: MainHandler;
}

interface ProjectAction {
    onEdit: () => void;
    onView: () => void;
}

export function ProjectList(props: ProjectListProps) {
    const { mainHandler } = props;

    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const [run, status, result, error] = useAsync<Project[]>();
    const projects = useMemo(() => (result === null ? [] : result), [result]);
    const isPending = () => {
        return status === State.pending;
    };
    const isFail = () => {
        return status === State.failure;
    };

    const load = useCallback(() => {
        if (token != null) {
            run(getAllProjectOfUser(token));
        }
    }, [run, token]);

    const columns: GridColDef[] = useMemo<GridColDef[]>(
        () => [
            { field: "name", headerName: "Name", width: 200, filterable: true },
            {
                field: "user",
                headerName: "User",
            },
            {
                field: "status",
                headerName: "Status",
                renderCell: (params: GridRenderCellParams<Deploy>) => {
                    return backendStatus(params.value);
                },
            },
            {
                field: "action",
                headerName: "Action",
                renderCell: (params: GridRenderCellParams<ProjectAction>) => {
                    const action =
                        params.value === undefined
                            ? {
                                  onEdit: () => {},
                                  onView: () => {},
                              }
                            : params.value;
                    return (
                        <Stack
                            direction="row"
                            justifyContent="flex-end"
                            alignItems="center"
                            divider={<Divider orientation="vertical" flexItem />}
                            spacing={0}
                        >
                            <IconButton onClick={action.onView}>
                                <AiOutlineEye />
                            </IconButton>
                            <IconButton onClick={action.onEdit}>
                                <AiOutlineEdit />
                            </IconButton>
                        </Stack>
                    );
                },
            },
        ],
        []
    );

    const rows = useMemo(() => {
        return projects.map((project: Project) => {
            return {
                id: project.id,
                name: project.name,
                user: project.user.username,
                status: project.deploy,
                action: {
                    onEdit: () => {
                        mainHandler?.openDesigner({
                            id: project.id,
                            mainHandler: mainHandler,
                        });
                    },
                    onView: () => {
                        mainHandler?.openDesigner({
                            id: project.id,
                            isReadOnly: true,
                        });
                    },
                },
            };
        });
    }, [mainHandler, projects]);

    const toolbar = () => {
        return (
            <GridToolbarContainer sx={{ width: "100%" }}>
                <Stack
                    direction="row"
                    justifyContent="flex-end"
                    alignItems="center"
                    spacing={0}
                    sx={{ width: "100%" }}
                >
                    <Divider orientation="vertical" flexItem />
                    <IconButton disabled={isPending()} onClick={load}>
                        <AiOutlineReload />
                        {isPending() && (
                            <CircularProgress
                                sx={{
                                    position: "absolute",
                                }}
                            />
                        )}
                    </IconButton>
                </Stack>
            </GridToolbarContainer>
        );
    };

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    useEffect(() => {
        load();
    }, [load]);

    return (
        <ThemeProvider theme={theme}>
            <div
                className={"panel"}
                style={{
                    display: "block",
                }}
            >
                {progress}
                {isFail() && <Alert severity={"error"}>{"Fail to load projects."}</Alert>}
                <Paper sx={{ height: "100%" }}>
                    <DataGrid
                        rows={rows}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        components={{
                            Toolbar: toolbar,
                        }}
                    />
                </Paper>
                <ResizeBackdrop open={isPending()} />
            </div>
        </ThemeProvider>
    );
}

