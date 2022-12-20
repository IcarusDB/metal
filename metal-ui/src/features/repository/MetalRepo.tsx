import { Accordion, AccordionDetails, AccordionSummary, Button, Divider, Typography } from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import Editor, { Monaco } from "@monaco-editor/react";
import * as EditorApi from "monaco-editor/esm/vs/editor/editor.api";
import { useEffect, useRef } from "react";
import { VscChevronDown, VscCloudUpload } from "react-icons/vsc";
import { useAsync } from "../../api/Hooks";
import { getAllMetalPkgsOfUserAccess } from "../../api/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { MetalPkg } from "../../model/MetalPkg";
import { tokenSelector } from "../user/userSlice";
import { Mutable } from "../../model/Mutable";

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
                <Typography variant="h5">
                    Repository Explorer
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <DataGrid
                    columns={columns}
                    rows={pkgs === null ? [] : pkgs}
                    pageSize={10}
                    rowsPerPageOptions={[10]}
                    autoHeight={true}
                />
            </AccordionDetails>
        </Accordion>
    );
}

interface MetalRepoMaintainProps {
    token: string | null
}

interface TextValue {
    value: () => string
}

class MutableTextValue extends Mutable<TextValue> implements TextValue{
    value() {
        return this.get().value();
    }
}

function MetalRepoMaintain(props: MetalRepoMaintainProps) {
    const {token} = props;
    const manifestRef = useRef<MutableTextValue>(new MutableTextValue({
        value: ()=>("")
    }))

    const handleDidMount = (editor: EditorApi.editor.IStandaloneCodeEditor, monaco: Monaco) => {
        manifestRef.current.set({
            value: () => {
                const model = editor.getModel();
                if (model === null) {
                    return "";
                } else {
                    return model.getValue();
                }
            }
        })
    }

    

    return (
        <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
                <Typography variant="h5">
                    Maintain
                </Typography>
            </AccordionSummary>
            <AccordionDetails
                sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "flex-start",
                    justifyContent: "center",
                }}
            >
            <Editor
                height={"60vh"}
                defaultLanguage={"json"}
                theme={"vs-dark"}
                onMount={handleDidMount}
            />
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
                >
                    Upload New Metal
                </Button>
            </div>
            </AccordionDetails>
        </Accordion>
    )
}
