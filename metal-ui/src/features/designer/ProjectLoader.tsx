import { useEffect } from "react";
import { Alert, LinearProgress } from "@mui/material";
import { Project } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../../api/ProjectApi";
import { useBackendStatus, useDeploy, useProfile, useSpec } from "./DesignerProvider";

function useProjectLoader(token: string | null, id: string) {
    console.log("Project loader.");
    const [run, status, project, error] = useAsync<Project>();
    const [, setSpec] = useSpec();
    const [, setProfile] = useProfile();
    const [, setDeploy] = useDeploy();
    const [, setBackendStatus] = useBackendStatus();

    useEffect(() => {
        if (token === null || id.trim() === "") {
            return;
        }
        if (status === State.success) {  
            if (project !== null) {
                setSpec(project.spec);
                setProfile(project.name, project.deploy.pkgs, project.deploy.platform, project.deploy.backend.args);
                setDeploy(project.deploy.id, project.deploy.epoch);
                setBackendStatus(project.deploy.backend.status);
            }
        }
        if (status === State.idle) {
            run(getProjectById(token, id));       
        }
        
    }, [id, project, run, setBackendStatus, setDeploy, setProfile, setSpec, status, token]);

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
