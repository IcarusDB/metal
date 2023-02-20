import { Alert, Button, Chip, Container, Divider, Grid, LinearProgress, List, ListItem, Paper, Typography } from "@mui/material";
import { useCallback, useEffect } from "react";
import { RiDeviceRecoverLine } from "react-icons/ri";
import { FiDelete } from "react-icons/fi";
import { VscEye } from "react-icons/vsc";
import { getExecOfId, recoverProjectFromExec, RecoverResponse, removeExec, RemoveExecResponse } from "../../api/ExecApi";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec, ExecState } from "../../model/Exec";
import { MainHandler } from "../main/Main";
import { useUIAsync } from "../ui/UIHooks";
import { tokenSelector } from "../user/userSlice";
import { ExecLoader } from "./ExecLoader";
import moment from "moment";

function useExecution(token: string | null, id: string): [() => void, State, Exec | null] {
    const [run, status, exec] = useUIAsync<Exec>();
    const getExec = useCallback(() => {
        if (token === null) {
            return;
        }
        run(getExecOfId(token, id));
    }, [id, run, token]);

    useEffect(()=>{
        if (status === State.idle) {
            getExec();
        }
        
    }, [getExec, status]);

    return [getExec, status, exec];
}

function useExecutionRecover(token: string | null, id: string): [
    () => void,
    State
] {
    const [run, status] = useUIAsync<RecoverResponse>();
    const recover = useCallback(()=>{
        if (token === null) {
            return;
        }
        run(recoverProjectFromExec(token, id));
    }, [id, run, token]);

    return [recover, status];
}

function useExecutionRemove(token: string | null, id: string): [()=>void, State] {
    const [run, status] = useUIAsync<RemoveExecResponse>();
    const remove = useCallback(()=>{
        if (token === null) {
            return;
        }
        run(removeExec(token, id));
    }, [id, run, token]);

    return [remove, status];
}

export interface ExecutionPageProps {
    id: string;
    mainHandler: MainHandler;
}

export function ExecutionPage(props: ExecutionPageProps) {
    const { id, mainHandler } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [getExec, getExecStatus, exec] = useExecution(token, id);
    const [recover, recoverStatus] = useExecutionRecover(token, id);
    const [remove, removeStatus] = useExecutionRemove(token, id);

    const isPending = getExecStatus === State.pending ||
                      recoverStatus === State.pending ||
                      removeStatus === State.pending;
         

    const recoverTip = recoverStatus === State.failure
                        ? "Fail to recover project"
                        : recoverStatus === State.success? "Success to recover project": "";
                        

    const removeTip = removeStatus === State.failure
                        ? "Fail to remove"
                        : removeStatus === State.success? "Success to remove.": "";

    const isGot = getExecStatus === State.success;

    const isRemoved = removeStatus === State.success;
    const isDisable = !isGot || isRemoved;

    const progress = isPending ? <LinearProgress /> : "";

    const onView = useCallback(()=>{
        if (exec === null) {
            return;
        }
        mainHandler.openViewer({
            id: exec.id,
            mainHandler: mainHandler,
            children: (
                <ExecLoader
                    token={token}
                    id={exec.id}
                    // name={exec.fromProjectDetail.name}
                />
            ),
        });
    }, [exec, mainHandler, token]);

    const onRecover = useCallback(()=>{
        recover();
    }, [recover]);

    const onRemove = useCallback(()=>{
        remove();
    }, [remove]);

    return (
        <div
            style={{
                boxSizing: "border-box",
                paddingLeft: "1vw",
                paddingRight: "1vw",
                paddingTop: "1vh",
                paddingBottom: "1vh",
            }}
        >
            {progress}
            <Typography variant="h6" color={"text.secondary"}>
                {`Execution[${id}]`}
            </Typography>
            {getExecStatus === State.failure && 
                <Alert 
                severity={"warning"}
                variant={"outlined"}
            >
                {"Fail to get execution."}
            </Alert>
            }
            {
                (recoverStatus === State.success || recoverStatus === State.failure) &&
                <Alert 
                    severity={recoverStatus === State.success? "info": "error"}
                    variant={"outlined"}
                >
                    {recoverTip}
                </Alert>
            }
            {
                (removeStatus === State.success || removeStatus === State.failure) && 
                <Alert
                    severity={removeStatus === State.success? "info": "error"}
                    variant={"outlined"}
                >
                    {removeTip}
                </Alert>
            }
            <List dense disablePadding={true}>
                <ListItem key="view" >
                    <Button 
                        startIcon={<VscEye />} 
                        onClick={onView} 
                        disabled={isDisable}
                    >
                        Browser
                    </Button>
                </ListItem>
                <ListItem key="recover">
                    <Button 
                        startIcon={<RiDeviceRecoverLine />} 
                        onClick={onRecover}
                        disabled={isDisable}
                    >
                        Recover one project
                    </Button>
                </ListItem>
                <ListItem key="delete">
                    <Button 
                        startIcon={<FiDelete />} 
                        onClick={onRemove} 
                        disabled={isDisable}
                    >
                        Delete
                    </Button>
                </ListItem>
            </List>
            <Divider orientation={'horizontal'} />
            <Paper
                sx={{
                    boxSizing: "border-box",
                    marginTop: "2vh",
                    padding: "1em",
                }}
                square
            >
            <Grid  container spacing={1}>
                <Grid key={"Status"} item xs={2}>{"Status"}</Grid>
                <Grid key={"statusVal"} item xs={10}>
                {
                    <Chip 
                        label={exec?.status} 
                        variant={exec?.status === ExecState.RUNNING || exec?.status === ExecState.FINISH? "filled": "outlined"} 
                        color={
                            exec?.status === ExecState.CREATE
                            ? "info"
                            : exec?.status === ExecState.FAILURE
                            ? "error"
                            : exec?.status === ExecState.FINISH
                            ? "success"
                            : exec?.status === ExecState.RUNNING
                            ? "info"
                            : "warning"
                        } 
                    />
                }
                </Grid>

                <Grid key={"createTime"} item xs={2}>{"Create Time"}</Grid>
                <Grid key={"createTimeVal"} item xs={10}>{moment(exec?.createTime).format("YYYY-MM-DD HH:mm:ss")}</Grid>

                <Grid key={"submitTime"} item xs={2}>{"Submit Time"}</Grid>
                <Grid key={"submitTimeVal"} item xs={10}>{moment(exec?.submitTime).format("YYYY-MM-DD HH:mm:ss")}</Grid>
                
                <Grid key={"beatTime"} item xs={2}>{"Beat Time"}</Grid>
                <Grid key={"beatTimeVal"} item xs={10}>{moment(exec?.beatTime).format("YYYY-MM-DD HH:mm:ss")}</Grid>
                
                <Grid key={"finishTime"} item xs={2}>{"Finish Time"}</Grid>
                <Grid key={"finishTimeVal"} item xs={10}>
                {
                    exec?.status === ExecState.FINISH? moment(exec?.finishTime).format("YYYY-MM-DD HH:mm:ss"): ""
                }
                </Grid>
                
                <Grid key={"terminateTime"} item xs={2}>{"Terminate Time"}</Grid>
                <Grid key={"terminateTimeVal"} item xs={10}>
                {
                    exec?.status === ExecState.FAILURE? moment(exec?.terminateTime).format("YYYY-MM-DD HH:mm:ss"): ""
                }
                </Grid>
            </Grid>
            </Paper>
            

        </div>
    );
}
