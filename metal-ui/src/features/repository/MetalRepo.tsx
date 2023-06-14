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
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Chip,
  Divider,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";
import { LoadingButton } from "@mui/lab";
import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import Editor, { Monaco } from "@monaco-editor/react";
import * as EditorApi from "monaco-editor/esm/vs/editor/editor.api";
import { useCallback, useEffect, useRef, useState } from "react";
import { VscBrowser, VscChevronDown, VscCloudUpload } from "react-icons/vsc";
import { useAsync } from "../../api/Hooks";
import {
  addMetalPkgsFromManifest,
  getAllMetalPkgsOfUserAccess,
} from "../../api/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import {
  MetalManifest,
  MetalPkg,
  Scope,
  validMainfest,
} from "../../model/MetalPkg";
import { tokenSelector } from "../user/userSlice";
import { Mutable } from "../../model/Mutable";
import { State } from "../../api/State";
import { Logger, loggerSelector, useNotice } from "../notice/Notice";
import shallow from "zustand/shallow";
import { MainHandler } from "../main/Main";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { vscDarkPlus } from "react-syntax-highlighter/dist/esm/styles/prism";
import moment from "moment";
export interface MetalRepoProps {
  mainHandler: MainHandler;
}

export function MetalRepo(props: MetalRepoProps) {
  const { mainHandler } = props;
  const token: string | null = useAppSelector((state) => {
    return tokenSelector(state);
  });

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
      <MetalRepoViewer token={token} mainHandler={mainHandler} />
      <MetalRepoMaintain token={token} />
    </div>
  );
}

export interface MetalPkgPageProps {
  pkg: MetalPkg;
  mainHandler: MainHandler;
}

export function MetalPkgPage(props: MetalPkgPageProps) {
  const { pkg } = props;
  const { id } = pkg;
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
      <Typography variant="h6" color={"text.secondary"}>
        {`${pkg.type} [${id}]`}
      </Typography>
      <Divider orientation={"horizontal"} />
      <Paper
        sx={{
          boxSizing: "border-box",
          marginTop: "2vh",
          padding: "1em",
        }}
        variant={"outlined"}
        square
      >
        <Grid container spacing={1}>
          <Grid item xs={1}>
            {"Class"}
          </Grid>
          <Grid item xs={11}>
            <Chip label={pkg.class} color="info" variant="filled" />
          </Grid>
          <Grid item xs={1}>
            {"Package"}
          </Grid>
          <Grid item xs={11}>
            <Stack
              direction="row"
              justifyContent="flex-start"
              alignItems="center"
              spacing={1}
            >
              <Chip label={pkg.groupId} color="info" variant="outlined" />
              <Chip label={pkg.artifactId} color="info" variant="filled" />
              <Chip label={pkg.version} color="success" variant="outlined" />
            </Stack>
          </Grid>
          <Grid item xs={1}>
            {"Scope"}
          </Grid>
          <Grid item xs={11}>
            {pkg.scope}
          </Grid>
          <Grid item xs={1}>
            {"Create Time"}
          </Grid>
          <Grid item xs={11}>
            {moment(pkg.createTime).format("YYYY-MM-DD HH:mm:ss")}
          </Grid>
          <Grid item xs={1}>
            {"Publisher"}
          </Grid>
          <Grid item xs={11}>
            {pkg.userId}
          </Grid>
          <Grid item xs={1}>
            {"Description"}
          </Grid>
          <Grid item xs={11}>
            {pkg.description}
          </Grid>
          <Grid item xs={1}>
            {"Form Schema"}
          </Grid>
          <Grid item xs={11}>
            <div
              style={{
                position: "relative",
                width: "100%",
                height: "100%",
                overflow: "hidden",
              }}
            >
              <div
                style={{
                  position: "relative",
                  maxHeight: "30vh",
                  width: "100%",
                  overflow: "auto",
                }}
              >
                <SyntaxHighlighter language={"json"} style={vscDarkPlus}>
                  {JSON.stringify(pkg.formSchema, null, 2)}
                </SyntaxHighlighter>
              </div>
            </div>
          </Grid>
          <Grid item xs={1}>
            {"UI Schema"}
          </Grid>
          <Grid item xs={11}>
            <div
              style={{
                position: "relative",
                width: "100%",
                height: "100%",
                overflow: "hidden",
              }}
            >
              <div
                style={{
                  position: "relative",
                  maxHeight: "30vh",
                  width: "100%",
                  overflow: "auto",
                }}
              >
                <SyntaxHighlighter language={"json"} style={vscDarkPlus}>
                  {JSON.stringify(pkg.uiSchema, null, 2)}
                </SyntaxHighlighter>
              </div>
            </div>
          </Grid>
        </Grid>
      </Paper>
    </div>
  );
}

interface MetalRepoViewerProps {
  token: string | null;
  mainHandler: MainHandler;
}

const columns: GridColDef[] = [
  { field: "type", headerName: "Type", filterable: true },
  { field: "scope", headerName: "Scope", filterable: true },
  { field: "class", headerName: "Class", filterable: true, minWidth: 400 },
  { field: "groupId", headerName: "Group ID", filterable: true, minWidth: 150 },
  {
    field: "artifactId",
    headerName: "Artifact ID",
    filterable: true,
    minWidth: 200,
  },
  { field: "version", headerName: "Version", filterable: true, minWidth: 150 },
  {
    field: "action",
    headerName: "Action",
    filterable: false,
    minWidth: 150,
    renderCell: (params: GridRenderCellParams<() => void>) => {
      return (
        <IconButton onClick={params.value}>
          <VscBrowser />
        </IconButton>
      );
    },
  },
];

function useMetalPkgs(token: string | null) {
  const [run, status, result, error] = useAsync<MetalPkg[]>();

  useEffect(() => {
    if (token !== null) {
      run(getAllMetalPkgsOfUserAccess(token));
    }
  }, [run, token]);
  return result;
}

function MetalRepoViewer(props: MetalRepoViewerProps) {
  const { token, mainHandler } = props;
  const pkgs: MetalPkg[] | null = useMetalPkgs(token);
  const pkgsWithAciton =
    pkgs === null
      ? []
      : pkgs.map((pkg) => ({
          ...pkg,
          action: () => {
            mainHandler.openMetalPkgPage({
              pkg: pkg,
              mainHandler: mainHandler,
            });
          },
        }));

  return (
    <Accordion defaultExpanded>
      <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
        <Typography variant="h5">Repository Explorer</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <Divider flexItem orientation="horizontal" />
        <div
          style={{
            boxSizing: "border-box",
            paddingTop: "1vh",
            width: "100%",
            display: "flex",
            flexDirection: "row",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <DataGrid
            columns={columns}
            rows={pkgsWithAciton}
            pageSize={10}
            rowsPerPageOptions={[10]}
            autoHeight={true}
          />
        </div>
      </AccordionDetails>
    </Accordion>
  );
}

function useMetalPkgsUpload(
  token: string | null
): [(scope: Scope, manifest: MetalManifest) => void, State, any] {
  const [run, status, result, error] = useAsync<void>();
  const upload = useCallback(
    (scope: Scope, manifest: MetalManifest) => {
      if (token !== null) {
        run(addMetalPkgsFromManifest(token, scope, manifest));
      }
    },
    [run, token]
  );

  return [upload, status, error];
}

interface MetalRepoMaintainProps {
  token: string | null;
}

interface TextValue {
  value: () => string;
}

class MutableTextValue extends Mutable<TextValue> implements TextValue {
  value() {
    return this.get().value();
  }
}

function uploadTip(status: State) {
  if (status === State.failure) {
    return (
      <Alert severity="error" variant="filled">
        Fail to upload new metals.
      </Alert>
    );
  }
  if (status === State.success) {
    return (
      <Alert severity="success" variant="filled">
        Success to upload new metals.
      </Alert>
    );
  }
  return <></>;
}

function MetalRepoMaintain(props: MetalRepoMaintainProps) {
  const notice = useNotice<Logger>(loggerSelector, shallow);
  const { token } = props;
  const [upload, uploadStatus, uploadError] = useMetalPkgsUpload(token);
  const [scope, setScope] = useState<Scope>(Scope.PRIVATE);
  const [error, setError] = useState<string | null>(null);
  const manifestRef = useRef<MutableTextValue>(
    new MutableTextValue({
      value: () => "",
    })
  );

  const handleDidMount = (
    editor: EditorApi.editor.IStandaloneCodeEditor,
    monaco: Monaco
  ) => {
    manifestRef.current.set({
      value: () => {
        const model = editor.getModel();
        if (model === null) {
          return "";
        } else {
          return model.getValue();
        }
      },
    });
  };

  const onScopeChange = (event: SelectChangeEvent) => {
    const scope = event.target.value as Scope;
    setScope(scope);
  };

  const onUploadNewMetals = () => {
    const manifestValue = manifestRef.current.value();
    if (manifestValue.trim() === "") {
      const msg = "Your input manifest is empty!";
      setError(msg);
      notice.warning(msg);
      return;
    }
    try {
      const manifest: MetalManifest = JSON.parse(manifestValue);
      if (!validMainfest(manifest)) {
        const msg = "Your input manifest is not passed validated!";
        setError(msg);
        notice.warning(msg);
        return;
      } else {
        setError(null);
      }
      upload(scope, manifest);
    } catch (err) {
      console.error(err);
      const msg = "Fail to parse your input manifest!";
      setError(msg);
      notice.error(msg);
    }
  };

  return (
    <Accordion defaultExpanded>
      <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
        <Typography variant="h5">Maintain</Typography>
      </AccordionSummary>

      <AccordionDetails
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "flex-start",
          justifyContent: "center",
        }}
      >
        <Divider flexItem orientation="horizontal" />
        <div
          style={{
            boxSizing: "border-box",
            paddingBottom: "1vh",
            paddingTop: "1vh",
            width: "100%",
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <Alert
            severity="info"
            variant="outlined"
            sx={{ boxSizing: "border-box", width: "100%" }}
          >
            <Typography color={"text.secondary"}>
              Please input manifest about metals, which can be copied from
              manifest file generated by Metal Maven Tool.
            </Typography>
          </Alert>
          <Snackbar
            open={error !== null}
            anchorOrigin={{
              vertical: "top",
              horizontal: "center",
            }}
            autoHideDuration={3000}
            onClose={() => {
              setError(null);
            }}
          >
            <Alert severity="warning" variant="filled">
              {error}
            </Alert>
          </Snackbar>
          {uploadTip(uploadStatus)}
        </div>

        <Editor
          height={"40vh"}
          defaultLanguage={"json"}
          theme={"vs-dark"}
          onMount={handleDidMount}
        />

        <div
          style={{
            boxSizing: "border-box",
            paddingTop: "2vh",
            width: "100%",
            display: "flex",
            flexDirection: "row",
            justifyContent: "flex-start",
            alignItems: "center",
          }}
        >
          <FormControl
            sx={{
              width: "100%",
            }}
          >
            <InputLabel>Scope</InputLabel>
            <Select value={scope} label="Scope" onChange={onScopeChange}>
              <MenuItem value={Scope.PUBLIC}>{Scope.PUBLIC}</MenuItem>
              <MenuItem value={Scope.PRIVATE}>{Scope.PRIVATE}</MenuItem>
            </Select>
          </FormControl>
        </div>

        <Divider
          flexItem
          orientation="horizontal"
          sx={{
            paddingTop: "1vh",
          }}
        />

        <div
          style={{
            boxSizing: "border-box",
            paddingTop: "1vh",
            width: "100%",
            display: "flex",
            flexDirection: "row",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <LoadingButton
            loading={uploadStatus === State.pending}
            startIcon={<VscCloudUpload />}
            variant="contained"
            onClick={onUploadNewMetals}
          >
            Upload New Metal
          </LoadingButton>
        </div>
      </AccordionDetails>
    </Accordion>
  );
}
