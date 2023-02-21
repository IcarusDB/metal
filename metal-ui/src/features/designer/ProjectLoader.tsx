import { useEffect } from "react";
import { Alert, LinearProgress } from "@mui/material";
import { Project } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../../api/ProjectApi";
import { useBackendArgsFn, useBackendStatusFn, useDeployIdFn, useEpochFn, useNameFn, usePkgsFn, usePlatformFn, useProjectIdFn, useSpecFn } from "./DesignerProvider";

function useProjectLoader(token: string | null, id: string) {
    
    const [run, status, project, error] = useAsync<Project>();
    const [, setProjectId] = useProjectIdFn();
    const [,setSpec] = useSpecFn();
    const [, setName] = useNameFn();
    const [, setPkgs] = usePkgsFn();
    const [, setPlatform] = usePlatformFn();
    const [, setBackendArgs] = useBackendArgsFn();
    const [,setDeployId] = useDeployIdFn();
    const [,setEpoch] = useEpochFn();
    const [,setBackendStatus] = useBackendStatusFn();

    useEffect(() => {
        if (token === null || id.trim() === "") {
            return;
        }
        if (status === State.success) {  
            if (project !== null) {
                setProjectId(project.id);
                setSpec(project.spec);
                setName(project.name);
                setPkgs(project.deploy.pkgs);
                setPlatform(project.deploy.platform);
                setBackendArgs(project.deploy.backend.args);
                setDeployId(project.deploy.id);
                setEpoch(project.deploy.epoch);
                setBackendStatus(project.deploy.backend.status);
            }
        }
        if (status === State.idle) {
            run(getProjectById(token, id));       
        }
        
    }, [id, project, run, setBackendArgs, setBackendStatus, setDeployId, setEpoch, setName, setPkgs, setPlatform, setProjectId, setSpec, status, token]);

    return [status, error];
}

export interface ProjectLoaderProps {
    token: string | null;
    id: string;
}

export function ProjectLoader(props: ProjectLoaderProps) {
    const { token, id } = props;
    const [loadStatus, loadError] = useProjectLoader(token, id);

    const isPending = () => loadStatus === State.pending;
    const isFailure = () => loadStatus === State.failure;

    return (
        <div>
            {isPending() && <LinearProgress />}
            {isFailure() && <Alert severity={"error"}>{"Fail to load project."}</Alert>}
        </div>
    );
}
