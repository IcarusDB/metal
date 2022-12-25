import { Divider, IconButton, Stack } from "@mui/material";
import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import { useCallback, useEffect } from "react";
import { AiOutlineEye } from "react-icons/ai";
import { getAllExecsOfUser } from "../../api/ExecApi";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec } from "../../model/Exec";
import { MainHandler } from "../main/Main";
import { tokenSelector } from "../user/userSlice";
import { ExecLoader } from "./ExecLoader";


function useExecutions(token: string | null): [() => void, State, Exec[] | null] {
    const [run, status, result, error] = useAsync<Exec[]>();

    const load = useCallback(()=>{
        if (token !== null) {
            run(getAllExecsOfUser(token));
        }
    }, [run, token]);

    return [load, status, result];
}

interface ExecAction {
    onView: () => void;
}

type ExecRow = Exec & {projectName: string, action: ExecAction} 

const columns: GridColDef[] = [
    {field: "fromProject", headerName: "From Project", filterable: true},
    {field: "projectName", headerName: "Project Name", filterable: true},
    {field: "status", headerName: "Status", filterable: true},
    {field: "createTime", headerName: "Create Time", filterable: true},
    {
        field: "action", 
        headerName: "Action", 
        renderCell: (params: GridRenderCellParams<ExecAction>) => {
            const action = params.value === undefined? {
                onView: () => {}
            }: params.value
            
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
                </Stack>
            )
        }
    }
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
    const rows: ExecRow[] = execs === null ? [] : 
        execs.map(
            exec => ({
                ...exec, 
                projectName: exec.fromProjectDetail.name,
                action: {
                    onView: ()=>{
                        mainHandler.openViewer({
                            id: exec.id,
                            mainHandler: mainHandler,
                            children: <ExecLoader 
                                token={token} 
                                id={exec.id} 
                                name={exec.fromProjectDetail.name} />
                        })
                    }
                }
            })
        );
        

    useEffect(()=>{
        load();
    }, [load])

    return (
        <DataGrid 
            columns={columns}
            rows={rows}
            pageSize={10}
            rowsPerPageOptions={[10]}
        />
    )
}