/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import {
  Alert,
  CircularProgress,
  Divider,
  IconButton,
  LinearProgress,
  Paper,
  Stack,
} from "@mui/material";
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridToolbarContainer,
} from "@mui/x-data-grid";
import moment from "moment";
import { useCallback, useEffect } from "react";
import { AiOutlineEye, AiOutlineReload } from "react-icons/ai";
import { VscBrowser } from "react-icons/vsc";
import { getAllExecsOfUser } from "../../api/ExecApi";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { useAppSelector } from "../../app/hooks";
import { Exec } from "../../model/Exec";
import { MainHandler } from "../main/Main";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { tokenSelector } from "../user/userSlice";
import { ExecLoader } from "./ExecLoader";

function useExecutions(
  token: string | null
): [() => void, State, Exec[] | null] {
  const [run, status, result] = useAsync<Exec[]>();

  const load = useCallback(() => {
    if (token !== null) {
      run(getAllExecsOfUser(token));
    }
  }, [run, token]);

  return [load, status, result];
}

interface ExecAction {
  onView: () => void;
  onOpen: () => void;
}

type ExecRow = Exec & { projectName: string; action: ExecAction };

const columns: GridColDef[] = [
  { field: "fromProject", headerName: "From Project", filterable: true },
  { field: "projectName", headerName: "Project Name", filterable: true },
  { field: "status", headerName: "Status", filterable: true },
  {
    field: "createTime",
    headerName: "Create Time",
    filterable: true,
    width: 200,
    renderCell: (params: GridRenderCellParams<number>) => {
      return moment(params.value).format("YYYY-MM-DD HH:mm:ss");
    },
  },
  {
    field: "action",
    headerName: "Action",
    renderCell: (params: GridRenderCellParams<ExecAction>) => {
      const action: ExecAction =
        params.value === undefined
          ? {
              onView: () => {},
              onOpen: () => {},
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
          <IconButton onClick={action.onOpen}>
            <VscBrowser />
          </IconButton>
        </Stack>
      );
    },
  },
];

function extractProjectName(exec: Exec) {
  if (exec.fromProjectDetail) {
    return exec.fromProjectDetail.name;
  }
  return "Unknown";
}

export interface ExecutionsProps {
  mainHandler: MainHandler;
}

export function Executions(props: ExecutionsProps) {
  const { mainHandler } = props;

  const token: string | null = useAppSelector((state) => {
    return tokenSelector(state);
  });

  const [load, loadStatus, execs] = useExecutions(token);
  const rows: ExecRow[] =
    execs === null
      ? []
      : execs
          .sort((a, b) => b.createTime - a.createTime)
          .map((exec) => ({
            ...exec,
            projectName: extractProjectName(exec),
            action: {
              onView: () => {
                mainHandler.openViewer({
                  id: exec.id,
                  mainHandler: mainHandler,
                  children: (
                    <ExecLoader
                      token={token}
                      id={exec.id}
                      name={extractProjectName(exec)}
                    />
                  ),
                });
              },
              onOpen: () => {
                mainHandler.openExecutionPage({
                  id: exec.id,
                  mainHandler: mainHandler,
                });
              },
            },
          }));

  const isPending = () => loadStatus === State.pending;
  const isFail = () => loadStatus === State.failure;

  const progress = isPending() ? (
    <LinearProgress />
  ) : (
    <LinearProgress variant="determinate" value={0} />
  );
  const toolbar = () => {
    return (
      <GridToolbarContainer sx={{ width: "100%" }}>
        <Stack
          direction="row"
          justifyContent="flex-end"
          alignItems="center"
          spacing={0}
          sx={{ width: "100%" }}
        >
          <Divider orientation="vertical" flexItem />
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

  useEffect(() => {
    load();
  }, [load]);

  return (
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
          columns={columns}
          rows={rows}
          pageSize={10}
          rowsPerPageOptions={[10]}
          components={{
            Toolbar: toolbar,
          }}
        />
        ;
      </Paper>
      <ResizeBackdrop open={isPending()} />
    </div>
  );
}
