import {
    Alert,
    Button,
    Grid,
    IconButton,
    Input,
    LinearProgress,
    Paper,
    Stack,
    Step,
    StepLabel,
    Stepper,
    Switch,
    Typography,
} from "@mui/material";
import {
    DataGrid,
    GridColDef,
    GridSelectionModel,
    GridToolbarContainer,
    GridValidRowModel,
    useGridApiContext,
} from "@mui/x-data-grid";
import { Form } from "@rjsf/mui";
import { RJSFSchema } from "@rjsf/utils";
import validator from "@rjsf/validator-ajv8";
import { ResizeBackdrop } from "../ui/ResizeBackdrop";
import { ForwardedRef, forwardRef, useImperativeHandle, useMemo, useState } from "react";
import { VscArrowLeft, VscCheck, VscClose } from "react-icons/vsc";
import { PlatformType } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { MetalPkg } from "../../model/MetalPkg";
import _ from "lodash";
import { getAllMetalPkgsOfUserAccess } from "../designer/explorer/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { State } from "../../api/State";
import { IChangeEvent } from "@rjsf/core";

export interface ProjectBasicProfileValue {
    name: string;
    platfomr: string;
}
export interface ProjectBasicProfileProps {
    profile?: ProjectBasicProfileValue;
    onFinish?: (profile: ProjectBasicProfileValue) => void;
}

export function ProjectBasicProfile(props: ProjectBasicProfileProps) {
    const { profile, onFinish } = props;
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

    const onSubmit = (data: IChangeEvent<any, RJSFSchema, any>) => {
        const newProfile: ProjectBasicProfileValue = data.formData;
        if (onFinish !== undefined) {
            onFinish(newProfile);
        }
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
            <Form
                formData={profile === undefined ? {} : profile}
                schema={formSchema}
                validator={validator}
                onSubmit={onSubmit}
            >
                <Button type={"submit"} variant={"contained"}>
                    {"confirm"}
                </Button>
            </Form>
        </Paper>
    );
}

export interface PkgSelectorProps {
    profile?: PkgProfileValue;
    onFinish?: (profile: PkgProfileValue) => void;
}

export interface PkgProfileValue {
    packages: MetalPackage[];
}

export interface MetalPackage {
    id: string | number;
    groupId: string;
    artifactId: string;
    version: string;
    scope: "PRIVATE" | "PUBLIC";
}

export const PkgSelector = (props: PkgSelectorProps) => {
    const { profile, onFinish } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const [isAuto, setAuto] = useState(
        profile === undefined ? true : profile.packages.length === 0
    );
    const { run, status, result, error } = useAsync<MetalPkg[]>();

    const onSwitch = () => {
        setAuto(!isAuto);
        if (token !== null && isAuto) {
            run(getAllMetalPkgsOfUserAccess(token));
        }
    };

    const isLoading = () => status === State.pending;

    const packages: MetalPackage[] =
        result === null
            ? []
            : result.map((metalPkg: MetalPkg, index: number) => {
                  const pkg: MetalPackage = {
                      id: index,
                      groupId: metalPkg.groupId,
                      artifactId: metalPkg.artifactId,
                      version: metalPkg.version,
                      scope: metalPkg.scope,
                  };
                  return pkg;
              });

    const packagesUniq = _.sortedUniqBy(
        packages,
        (pkg) => pkg.groupId + ":" + pkg.artifactId + ":" + pkg.version
    );

    const initialSelectionModel: GridSelectionModel = packagesUniq
        .filter((pkg) => {
            if (profile === undefined) {
                return false;
            }
            const equalPkg = profile.packages.find((prevPkg) => {
                return (
                    prevPkg.groupId === pkg.groupId &&
                    prevPkg.artifactId === pkg.artifactId &&
                    prevPkg.version === pkg.version
                );
            });

            if (equalPkg === undefined) {
                return false;
            }
            return true;
        })
        .map((pkg) => pkg.id);

    const [selectionModel, setSelectionModel] = useState(initialSelectionModel);

    const mode = () => (isAuto ? "Auto mode." : "Custom mode.");

    const progress =
        status === State.pending ? (
            <LinearProgress />
        ) : (
            <LinearProgress variant="determinate" value={0} />
        );

    const columns: GridColDef[] = useMemo<GridColDef[]>(
        () => [
            { field: "groupId", headerName: "Group ID", width: 200, filterable: true },
            { field: "artifactId", headerName: "Artifact ID", width: 300 },
            { field: "version", headerName: "Version", width: 200 },
            { field: "scope", headerName: "Scope", width: 100 },
        ],
        []
    );

    const TblToolBar = () => {
        const dataGridApi = useGridApiContext();
        const selectedPackages = () => {
            let selectedPkgs: MetalPackage[] = [];
            dataGridApi.current.getSelectedRows().forEach((rowModel: GridValidRowModel) => {
                const selectedPkg: MetalPackage = {
                    id: rowModel["id"],
                    groupId: rowModel["groupId"],
                    artifactId: rowModel["artifactId"],
                    version: rowModel["version"],
                    scope: rowModel["scope"],
                };
                selectedPkgs.push(selectedPkg);
            });
            const newProfile: PkgProfileValue = {
                packages: selectedPkgs,
            };
            if (onFinish !== undefined) {
                onFinish(newProfile);
            }
        };
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
    };

    const tbl = useMemo(
        () => (
            <DataGrid
                rows={packagesUniq}
                columns={columns}
                selectionModel={selectionModel}
                onSelectionModelChange={(newSelectionModel) => {
                    setSelectionModel(newSelectionModel);
                }}
                pageSize={5}
                rowsPerPageOptions={[5]}
                checkboxSelection
                components={{
                    Toolbar: TblToolBar,
                }}
            />
        ),
        [columns, packagesUniq]
    );

    const onAutoConfirm = () => {
        if (onFinish !== undefined) {
            onFinish({
                packages: [],
            });
        }
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
                        {isAuto && (
                            <Button variant="contained" color="primary" onClick={onAutoConfirm}>
                                {"Confirm"}
                            </Button>
                        )}
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

const STEP_SIZE = 3;

export const ProjectProfile = forwardRef(
    (props: ProjectProfileProps, ref: ForwardedRef<ProjectProfileHandler>) => {
        const { open } = props;
        const [isOpen, setOpen] = useState(open);
        const [activeStep, setActiveStep] = useState(0);
        const [basicProfile, setBasicProfile] = useState<ProjectBasicProfileValue>();
        const [pkgProfile, setPkgProfile] = useState<PkgProfileValue>();

        const close = () => {
            setOpen(false);
        };

        const handleNextStep = () => {
            setActiveStep(activeStep + 1 > STEP_SIZE - 1 ? STEP_SIZE - 1 : activeStep + 1);
        };

        const handleBackStep = () => {
            setActiveStep(activeStep - 1 >= 0 ? activeStep - 1 : 0);
        };

        const onBasicProfileFinish = (newProfile: ProjectBasicProfileValue) => {
            setBasicProfile(newProfile);
            handleNextStep();
        };

        const onPkgProfileFinish = (newProfile: PkgProfileValue) => {
            setPkgProfile(newProfile);
            handleNextStep();
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
                            <VscClose />
                        </IconButton>
                    </Paper>

                    <Stepper activeStep={activeStep}>
                        <Step key={"Basic Profile."} completed={false}>
                            <StepLabel>{"Basic Profile."}</StepLabel>
                        </Step>
                        <Step key={"Package select."} completed={false}>
                            <StepLabel>{"Package select."}</StepLabel>
                        </Step>
                        <Step key={"Profile Finish."} completed={false}>
                            <StepLabel>{"Profile Finish."}</StepLabel>
                        </Step>
                    </Stepper>

                    <Stack
                        direction="column"
                        justifyContent="flex-start"
                        alignItems="stretch"
                        spacing={2}
                    >
                        {activeStep === 0 && (
                            <ProjectBasicProfile
                                profile={basicProfile}
                                onFinish={onBasicProfileFinish}
                            />
                        )}
                        {activeStep === 1 && (
                            <PkgSelector profile={pkgProfile} onFinish={onPkgProfileFinish} />
                        )}
                        {activeStep === 2 && (
                            <Alert variant="outlined" severity="success">
                                {"Profile Finish"}
                            </Alert>
                        )}
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
                            <IconButton onClick={handleBackStep}>
                                <VscArrowLeft />
                            </IconButton>
                        </Paper>
                    </Stack>
                </div>
            </ResizeBackdrop>
        );
    }
);
