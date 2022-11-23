import './Project.css';
import {useAppSelector} from "../../app/hooks";
import {tokenSelector} from "../user/userSlice";
import {
    Box,
    Paper,
    Divider,
    IconButton,
    List,
    ListItem,
    ListItemText,
    Tooltip,
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableBody,
    TableCell,
    CircularProgress, Alert, Backdrop, LinearProgress
} from "@mui/material";
import Stack from '@mui/material/Stack';
import {createTheme, ThemeProvider} from '@mui/material/styles';
import {
    AiOutlineStop,
    AiOutlineApi,
    AiFillThunderbolt,
    AiOutlineWarning,
    AiOutlineQuestionCircle,
    AiOutlineEye,
    AiOutlineEdit,
    AiOutlineReload
} from "react-icons/ai";
import {
    BsCommand
} from "react-icons/bs";
import {HiStop} from "react-icons/hi";
import {useCallback, useEffect, useState} from "react";
import {BackendState, BackendStatus, Deploy, Project} from "../../model/Project";
import {getAllProjectOfUser} from "./ProjectApi";
import { State } from '../../api/State';
import { useAsync } from '../../api/Hooks';
import { ResizeBackdrop } from '../ui/ResizeBackdrop';


function backendStatusTip(backendStatus: BackendStatus) {
    const upTime = backendStatus.upTime === undefined ? <></> : (
        <ListItem>
            <ListItemText>{'Up Time'}</ListItemText>
            <ListItemText>{backendStatus.upTime}</ListItemText>
        </ListItem>
    )

    const downTime = backendStatus.downTime === undefined ? <></> : (
        <ListItem>
            <ListItemText>{'Down Time'}</ListItemText>
            <ListItemText>{backendStatus.downTime}</ListItemText>
        </ListItem>
    )

    const failureTime = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <ListItem>
            <ListItemText>{'Failure Time'}</ListItemText>
            <ListItemText>{backendStatus.failureTime}</ListItemText>
        </ListItem>
    )

    const failureMsg = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <ListItem>
            <ListItemText>{'Failure Message'}</ListItemText>
            <ListItemText>{backendStatus.failureMsg}</ListItemText>
        </ListItem>
    )

    return (
        <List>
            <ListItem>
                <ListItemText>{'current'}</ListItemText>
                <ListItem>{backendStatus.current}</ListItem>
            </ListItem>
            {upTime}
            {downTime}
            {failureTime}
            {failureMsg}
        </List>
    )
}

function backendStatus(deploy: Deploy) {
    if (deploy.backend === undefined || deploy.backend.status === undefined) {
        return (
            <Tooltip title={'No deployment is set.'}>
                <IconButton>
                    <AiOutlineStop/>
                </IconButton>
            </Tooltip>
        )
    }

    switch (deploy.backend.status.current) {
        case BackendState.UN_DEPLOY: 
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <AiOutlineApi/>
                    </IconButton>
                </Tooltip>
            )

            ;
        case BackendState.UP: 
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <AiFillThunderbolt/>
                    </IconButton>
                </Tooltip>
            )
        
            ;
        case BackendState.DOWN: 
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <HiStop/>
                    </IconButton>
                </Tooltip>
            )
        
            ;
        case BackendState.FAILURE: 
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <IconButton>
                        <AiOutlineWarning/>
                    </IconButton>
                </Tooltip>
            )
        
            ;
        default: 
            return (
                <Tooltip title={'Unknown'}>
                    <AiOutlineQuestionCircle/>
                </Tooltip>
            )
        }
    
}

export function ProjectItem(props: { item: Project, index: number }) {
    const {item} = props;
    return (
        <TableRow key={item.id}>
            <TableCell>{item.name}</TableCell>
            <TableCell>{item.user.username}</TableCell>
            <TableCell>
                {backendStatus(item.deploy)}
            </TableCell>
            <TableCell>
                <Stack
                    direction="row"
                    justifyContent="flex-end"
                    alignItems="center"
                    divider={<Divider orientation="vertical" flexItem/>}
                    spacing={0}
                >
                    <IconButton><AiOutlineEye/></IconButton>
                    <IconButton><AiOutlineEdit/></IconButton>
                </Stack>
            </TableCell>
        </TableRow>
    )
}

const theme = createTheme()

export function ProjectList() {
    const token: string | null = useAppSelector(state => {
        return tokenSelector(state)
    })
    const [run, status, result, error] = useAsync<Project[]>()
    const projects = result === null? []: result;
    const isPending = () => {
        return status === State.pending
    }
    const isFail = () => {
        return status === State.failure
    }

    const load = useCallback(() => {
        if (token != null) {
            run(getAllProjectOfUser(token));
        }
    }, [run, token])

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    useEffect(() => {
        load();
    }, [load])

    return (
        <ThemeProvider theme={theme}>
            <div className={'panel'}
                 style={{flexDirection: "column", alignItems: "stretch", justifyContent: "flex-start"}}
            >
                
                <Stack
                    direction="column"
                    justifyContent="flex-start"
                    alignItems="stretch"
                    spacing={2}
                    divider={<Divider orientation="horizontal" flexItem/>}
                >
                    {isFail() &&
                        <Alert severity={"error"}>{"Fail to load projects."}</Alert>
                    }
                    <Box sx={{width: "100%"}}>
                        <Paper>
                            <Stack
                                direction="row"
                                justifyContent="flex-end"
                                alignItems="center"
                                divider={<Divider orientation="vertical" flexItem/>}
                                spacing={0}
                            >
                                <></>
                                <IconButton
                                    disabled={isPending()}
                                    onClick={load}
                                >
                                    <AiOutlineReload/>
                                    {isPending() &&
                                        <CircularProgress
                                            sx={{
                                                position: 'absolute'
                                            }}
                                        />
                                    }
                                </IconButton>
                            </Stack>
                            {progress}
                        </Paper>
                    </Box>

                    <TableContainer component={Paper}>
                        <Table
                            sx={{minWidth: 400}}
                            size={"small"}
                        >
                            <TableHead>
                                <TableRow>
                                    <TableCell>{"Name"}</TableCell>
                                    <TableCell>{"User"}</TableCell>
                                    <TableCell>{"Status"}</TableCell>
                                    <TableCell align={"right"}><BsCommand/></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {projects.map((item: Project, index: number) => {
                                    return ProjectItem({item: item, index: index})
                                })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Stack>
                <ResizeBackdrop open={isPending()} />
            </div>
        </ThemeProvider>
    )


}

