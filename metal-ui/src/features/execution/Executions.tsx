import { DataGrid, GridColDef } from "@mui/x-data-grid";
import { useCallback, useEffect } from "react";
import { getAllExecsOfUser } from "../../api/ExecApi";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec } from "../../model/Exec";
import { MainHandler } from "../main/Main";
import { tokenSelector } from "../user/userSlice";


function useExecutions(token: string | null): [() => void, State, Exec[] | null] {
    const [run, status, result, error] = useAsync<Exec[]>();

    const load = useCallback(()=>{
        if (token !== null) {
            run(getAllExecsOfUser(token));
        }
    }, [run, token]);

    return [load, status, result];
}

const columns: GridColDef[] = [
    {field: "fromProject", headerName: "From Project", filterable: true},
    {field: "projectName", headerName: "Project Name", filterable: true},
    {field: "status", headerName: "Status", filterable: true},
    {field: "createTime", headerName: "Create Time", filterable: true},
]

export interface ExecutionsProps {
    mainHandler: MainHandler;
}

export function Executions(props: ExecutionsProps) {
    const { mainHandler } = props;

    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const [load, loadStatus, execs] = useExecutions(token);

    useEffect(()=>{
        load();
    }, [load])

    return (
        <DataGrid 
            columns={columns}
            rows={execs === null ? [] : execs.map(exec => ({...exec, projectName: exec.fromProjectDetail.name}))}
            pageSize={10}
            rowsPerPageOptions={[10]}
            autoHeight={true}
        />
    )
}