import { Alert, LinearProgress } from "@mui/material";
import { useEffect } from "react";
import { getExecOfId } from "../../api/ExecApi";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec } from "../../model/Exec";
import { useProfile, useSpec } from "../designer/DesignerProvider";
import { MainHandler } from "../main/Main";
import { tokenSelector } from "../user/userSlice";

function useExecLoader(token: string | null, id: string, name?: string): [State, any] {
    const [, setSpec] = useSpec();
    const [, setProfile] = useProfile();
    const [run, status, exec, error] = useAsync<Exec>();


    useEffect(()=>{
        if (token === null || id.trim() === "") {
            return;
        }
        if (status === State.idle) {
            run(getExecOfId(token, id));
        }

        if (status === State.success && exec !== null) {
            setSpec(exec.SPEC);
            setProfile(
                name === undefined? `Project[${name}]-${exec.id}`: `Project[${exec.fromProject}]-${exec.id}`,
                exec.deploy.pkgs,
                exec.deploy.platform,
                exec.deploy.backend.args
            );
        }
    }, [exec, id, run, setProfile, setSpec, status, token]);

    return [status, error];
}

export interface ExecLoaderProps {
    id: string,
    token: string | null,
    mainHandler?: MainHandler,
    name?: string,
}

export function ExecLoader(props: ExecLoaderProps) {
    const {id, token, mainHandler, name} = props;

    const [loadStatus, loadError] = useExecLoader(token, id);
    const isPending = () => (loadStatus === State.pending);
    const isFailure = () => (loadStatus === State.failure);

    return (
        <div>
            {isPending() && <LinearProgress />}
            {isFailure() && <Alert severity={"error"}>{"Fail to load exec."}</Alert>}
        </div>
    )

}