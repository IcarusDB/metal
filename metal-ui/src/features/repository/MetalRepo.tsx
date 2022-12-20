import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Alert,
    Button,
    Divider,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Snackbar,
    Typography,
} from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import Editor, { Monaco } from "@monaco-editor/react";
import * as EditorApi from "monaco-editor/esm/vs/editor/editor.api";
import { useCallback, useEffect, useRef, useState } from "react";
import { VscChevronDown, VscCloudUpload } from "react-icons/vsc";
import { useAsync } from "../../api/Hooks";
import { addMetalPkgsFromManifest, getAllMetalPkgsOfUserAccess } from "../../api/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { MetalManifest, MetalPkg, Scope, validMainfest } from "../../model/MetalPkg";
import { tokenSelector } from "../user/userSlice";
import { Mutable } from "../../model/Mutable";
import { State } from "../../api/State";
import Ajv, { JSONSchemaType } from "ajv";

export interface MetalRepoProps {}

export function MetalRepo(props: MetalRepoProps) {
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
            <MetalRepoViewer token={token} />
            <MetalRepoMaintain token={token} />
        </div>
    );
}

interface MetalRepoViewerProps {
    token: string | null;
}

const columns: GridColDef[] = [
    { field: "type", headerName: "Type", filterable: true },
    { field: "scope", headerName: "Scope", filterable: true },
    { field: "class", headerName: "Class", filterable: true, minWidth: 400 },
    { field: "groupId", headerName: "Group ID", filterable: true, minWidth: 150 },
    { field: "artifactId", headerName: "Artifact ID", filterable: true, minWidth: 200 },
    { field: "version", headerName: "Version", filterable: true, minWidth: 150 },
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
    const { token } = props;
    const pkgs: MetalPkg[] | null = useMetalPkgs(token);

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
                        rows={pkgs === null ? [] : pkgs}
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
    const { token } = props;
    const [upload, uploadStatus, uploadError] = useMetalPkgsUpload(token);
    const [scope, setScope] = useState<Scope>(Scope.PRIVATE);
    const [error, setError] = useState<string | null>(null);
    const manifestRef = useRef<MutableTextValue>(
        new MutableTextValue({
            value: () => "",
        })
    );

    const handleDidMount = (editor: EditorApi.editor.IStandaloneCodeEditor, monaco: Monaco) => {
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
            setError("Your input manifest is empty!");
            return;
        }
        try {
            const manifest: MetalManifest = JSON.parse(manifestValue);
            if (!validMainfest(manifest)) {
                setError("Your input manifest is not passed validated!");
                return;
            } else {
                setError(null);
            }
            upload(scope, manifest);
        } catch (err) {
            console.error(err);
            setError("Fail to parse your input manifest!");
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
                    <Alert severity="info" variant="outlined">
                        <Typography color={"text.secondary"}>
                            Please input manifest about metals, which can be copied from manifest
                            file generated by Metal Maven Tool.
                        </Typography>
                    </Alert>
                    <Snackbar 
                        open={error !== null} 
                        anchorOrigin={{
                            vertical: "top",
                            horizontal: "center",
                        }}
                        autoHideDuration={3000}
                        onClose={()=>{
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
                    <Button
                        startIcon={<VscCloudUpload />}
                        variant="contained"
                        onClick={onUploadNewMetals}
                    >
                        Upload New Metal
                    </Button>
                </div>
            </AccordionDetails>
        </Accordion>
    );
}
