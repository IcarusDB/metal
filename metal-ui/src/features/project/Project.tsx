import "./Project.css";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import {
    Box,
    Paper,
    Divider,
    IconButton,
    List,
    ListItem,
    ListItemText,
    Tooltip,
    TableRow,
    TableCell,
    CircularProgress,
    Alert,
    LinearProgress,
    Container,
    Button,
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
import { useCallback, useEffect, useMemo, useRef } from "react";
import { BackendState, BackendStatus, Deploy, Project } from "../../model/Project";
import { getAllProjectOfUser } from "../../api/ProjectApi";
import { State } from "../../api/State";
import { useAsync } from "../../api/Hooks";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { MainHandler } from "../main/Main";
import { VscAdd } from "react-icons/vsc";
import { DataGrid, GridColDef, GridRenderCellParams, GridToolbarContainer } from "@mui/x-data-grid";

function backendStatusTip(backendStatus: BackendStatus) {
    const upTime =
        backendStatus.upTime === undefined ? (
            <></>
        ) : (
            <ListItem>
                <ListItemText>{"Up Time"}</ListItemText>
                <ListItemText>{backendStatus.upTime}</ListItemText>
            </ListItem>
        );

    const downTime =
        backendStatus.downTime === undefined ? (
            <></>
        ) : (
            <ListItem>
                <ListItemText>{"Down Time"}</ListItemText>
                <ListItemText>{backendStatus.downTime}</ListItemText>
            </ListItem>
        );

    const failureTime =
        backendStatus.failureTime === undefined ||
        backendStatus.current !== BackendState.FAILURE ? (
            <></>
        ) : (
            <ListItem>
                <ListItemText>{"Failure Time"}</ListItemText>
                <ListItemText>{backendStatus.failureTime}</ListItemText>
            </ListItem>
        );

    const failureMsg =
        backendStatus.failureTime === undefined ||
        backendStatus.current !== BackendState.FAILURE ? (
            <></>
        ) : (
            <ListItem>
                <ListItemText>{"Failure Message"}</ListItemText>
                <ListItemText>{backendStatus.failureMsg}</ListItemText>
            </ListItem>
        );

    return (
        <List>
            <ListItem>
                <ListItemText>{"current"}</ListItemText>
                <ListItem>{backendStatus.current}</ListItem>
            </ListItem>
            {upTime}
            {downTime}
            {failureTime}
            {failureMsg}
        </List>
    );
}

function backendStatus(deploy: Deploy | undefined) {
    if (deploy === undefined) {
        return "?";
    }
    if (deploy.backend === undefined || deploy.backend.status === undefined) {
        return (
            <Tooltip title={"No deployment is set."}>
                <IconButton>
                    <AiOutlineStop />
                </IconButton>
            </Tooltip>
        );
    }

    switch (deploy.backend.status.current) {
        case BackendState.UN_DEPLOY:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <AiOutlineApi />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.UP:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <AiFillThunderbolt />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.DOWN:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <HiStop />
                    </IconButton>
                </Tooltip>
            );
        case BackendState.FAILURE:
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
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
    const starterCounter = useRef(0);

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
                            name: project.name,
                            mainHandler: mainHandler
                        });
                    },
                    onView: () => {
                        mainHandler?.openDesigner({
                            id: project.id,
                            name: project.name,
                            isReadOnly: true,
                        })
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
                    justifyContent="space-between"
                    alignItems="center"
                    divider={<Divider orientation="vertical" flexItem />}
                    spacing={0}
                    sx={{ width: "100%" }}
                >
                    <Button variant="contained" onClick={onAddProject}>
                        <VscAdd />
                    </Button>

                    <Container></Container>
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

    const onAddProject = () => {
        mainHandler.openProjectStarter({
            id: `starter[${starterCounter.current++}]`,
            mainHandler: mainHandler,
        });
    };

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
