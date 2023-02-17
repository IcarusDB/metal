import { Button, LinearProgress, List, ListItem, Typography } from "@mui/material";
import { useCallback, useEffect } from "react";
import { RiDeviceRecoverLine } from "react-icons/ri";
import { FiDelete } from "react-icons/fi";
import { VscEye } from "react-icons/vsc";
import { getExecOfId } from "../../api/ExecApi";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec } from "../../model/Exec";
import { MainHandler } from "../main/Main";
import { useUIAsync } from "../ui/UIHooks";
import { tokenSelector } from "../user/userSlice";
import { ExecLoader } from "./ExecLoader";

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
    const isPending = getExecStatus === State.pending;
    const progress = isPending ? <LinearProgress /> : "";

    const onView = useCallback(()=>{
        if (exec === null) {
            return;
        }
        console.log(exec);
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
            <List dense disablePadding={true}>
                <ListItem key="view" >
                    <Button startIcon={<VscEye />} onClick={onView}>
                        Browser
                    </Button>
                </ListItem>
                <ListItem key="recover">
                    <Button startIcon={<RiDeviceRecoverLine />} >
                        Recover one project
                    </Button>
                </ListItem>
                <ListItem key="delete">
                    <Button startIcon={<FiDelete />} >
                        Delete
                    </Button>
                </ListItem>
            </List>
        </div>
    );
}
