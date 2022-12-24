import { useEffect } from "react";
import { Alert, LinearProgress } from "@mui/material";
import { Project } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { getProjectById } from "../../api/ProjectApi";
import { useProfile, useSpec } from "./DesignerProvider";

function useProjectLoader(token: string | null, id: string) {
    const [run, status, result, error] = useAsync<Project>();
    const [, setSpec] = useSpec();
    const [, setProfile] = useProfile();

    useEffect(() => {
        if (token === null || id.trim() === "") {
            return;
        }
        run(
            getProjectById(token, id).then(proj => {
                setSpec(proj.spec);
                setProfile(proj.name, proj.deploy.pkgs, proj.deploy.platform, proj.deploy.backend.args);
                return proj;
            })
        );
    }, [id, run, setProfile, setSpec, token]);

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
