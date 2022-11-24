import { Button, Grid, IconButton, LinearProgress, Paper, Switch, Typography } from "@mui/material";
import { DataGrid, GridColDef, GridToolbarContainer, GridValidRowModel, useGridApiContext } from "@mui/x-data-grid";
import { Form } from "@rjsf/mui";
import { RJSFSchema } from "@rjsf/utils";
import validator from "@rjsf/validator-ajv8";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { ForwardedRef, forwardRef, useImperativeHandle, useMemo, useState } from "react";
import { VscArrowLeft, VscCheck } from "react-icons/vsc";
import { PlatformType } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { MetalPkg } from "../../model/MetalPkg";
import _ from "lodash";
import { getAllMetalPkgsOfUserAccess } from "../designer/explorer/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { State } from "../../api/State";

export function ProjectBasicProfile() {
    const formSchema: RJSFSchema = {
        type: "object",
        required: ["name", "platform"],
        properties: {
            name: {
                type: "string",
                title: "Project Name",
            },
            platform: {
                type: "string",
                title: "Platform Type",
                enum: Object.values(PlatformType),
            },
        },
    };

    return (
        <Paper
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Form schema={formSchema} validator={validator}></Form>
        </Paper>
    );
}

export const PkgSelector = () => {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [isAuto, setAuto] = useState(true);
    const { run, status, result, error } = useAsync<MetalPkg[]>();

    const onSwitch = () => {
        setAuto(!isAuto);
        if (token !== null && isAuto) {
            run(getAllMetalPkgsOfUserAccess(token));
        }
    };

    const isLoading = () => status === State.pending;

    const packages =
        result === null
            ? []
            : result.map((metalPkg: MetalPkg, index: number) => {
                  return {
                      id: index,
                      groupId: metalPkg.groupId,
                      artifactId: metalPkg.artifactId,
                      version: metalPkg.version,
                      scope: metalPkg.scope,
                  };
              });

    const packagesUniq = _.sortedUniqBy(
        packages,
        (pkg) => pkg.groupId + ":" + pkg.artifactId + ":" + pkg.version
    );

    const mode = () => (isAuto ? "Auto mode." : "Custom mode.");

    const progress = (status === State.pending)? <LinearProgress/>: <LinearProgress variant="determinate" value={0} />

    const columns: GridColDef[] = useMemo<GridColDef[]>(
        () => [
            { field: "groupId", headerName: "Group ID", width: 200, filterable: true },
            { field: "artifactId", headerName: "Artifact ID", width: 300 },
            { field: "version", headerName: "Version", width: 200 },
            { field: "scope", headerName: "Scope", width: 100 },
        ],
        []
    );

    const TblToolBar = ()=>{
        const dataGridApi = useGridApiContext();
        const selectedPackages = () => {
            dataGridApi.current.getSelectedRows()
            .forEach((rowModel: GridValidRowModel) => {
                console.log(rowModel)
            })
        }
        return (
            <GridToolbarContainer
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    justifyContent: "flex-end",
                    alignContent: "center",
                }}
            >
                <Button variant="contained" color="primary" onClick={selectedPackages}>
                    {"Confirm"}
                </Button>
            </GridToolbarContainer>
        );
    }

    const tbl = useMemo(()=>(
        <DataGrid
            rows={packagesUniq}
            columns={columns}
            pageSize={5}
            rowsPerPageOptions={[5]}
            checkboxSelection
            components={{
                Toolbar: TblToolBar,
            }}
        />
    ), [columns, packagesUniq]);

    return (
        <Paper
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Grid
                container
                spacing={0}
                sx={{
                    display: "flex",
                    flexDirection: "row",
                    height: "100%",
                    width: "100%",
                    alignContent: "flex-start",
                    justifyContent: "flex-start",
                }}
            >
                <Grid item xs={12}>
                    <Typography variant="h6">{"Metal Package Selector"}</Typography>
                </Grid>
                <Grid item xs={12}>
                    <Paper
                        square
                        variant="outlined"
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            flexWrap: "wrap",
                            alignContent: "center",
                            justifyContent: "flex-start",
                            alignItems: "center",
                        }}
                    >
                        <Switch checked={isAuto} onChange={onSwitch} />
                        <Typography variant="body1">{mode()}</Typography>
                    </Paper>
                </Grid>
                <Grid
                    item
                    xs={12}
                    sx={{
                        boxSizing: "border-box",
                        margin: "0px",
                        width: "100%",
                        height: "70%",
                        position: "relative",
                    }}
                >
                    {!isAuto && progress}
                    {!isAuto && tbl}
                    <ResizeBackdrop open={isLoading()} />
                </Grid>
            </Grid>
        </Paper>
    );
};

export interface ProjectProfileProps {
    open: boolean;
}

export interface ProjectProfileHandler {
    open: () => void;
    close: () => void;
}

export const ProjectProfile = forwardRef(
    (props: ProjectProfileProps, ref: ForwardedRef<ProjectProfileHandler>) => {
        const { open } = props;
        const [isOpen, setOpen] = useState(open);

        const close = () => {
            setOpen(false);
        };

        useImperativeHandle(
            ref,
            () => ({
                open: () => {
                    setOpen(true);
                },
                close: close,
            }),
            []
        );

        return (
            <ResizeBackdrop open={isOpen} backgroundColor={"#f4f4f4"} opacity={"1"}>
                <div
                    style={{
                        position: "absolute",
                        boxSizing: "border-box",
                        margin: "0px",
                        width: "100%",
                        height: "100%",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "flex-start",
                        justifyContent: "flex-start",
                    }}
                >
                    <Paper
                        square
                        variant="outlined"
                        sx={{
                            boxSizing: "border-box",
                            margin: "0px",
                            width: "100%",
                            height: "5vh",
                            display: "flex",
                            flexDirection: "row",
                            alignContent: "space-between",
                            justifyContent: "flex-start",
                        }}
                    >
                        <IconButton onClick={close}>
                            <VscArrowLeft />
                        </IconButton>
                    </Paper>
                    {/* <ProjectBasicProfile />  */}
                    <PkgSelector />
                </div>
            </ResizeBackdrop>
        );
    }
);
