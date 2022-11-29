import {
    Alert,
    Button,
    Container,
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
import {
    ForwardedRef,
    forwardRef,
    useEffect,
    useImperativeHandle,
    useMemo,
    useRef,
    useState,
} from "react";
import { VscArrowLeft, VscCheck, VscClose, VscInfo } from "react-icons/vsc";
import { platformSchema, platformType, PlatformType } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { MetalPkg } from "../../model/MetalPkg";
import _ from "lodash";
import { getAllMetalPkgsOfUserAccess } from "../designer/explorer/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { State } from "../../api/State";
import { IChangeEvent } from "@rjsf/core";
import Editor, { EditorProps, Monaco, OnMount, useMonaco } from "@monaco-editor/react";
import * as EditorApi from "monaco-editor/esm/vs/editor/editor.api";

export interface ProjectBasicProfileValue {
    name: string;
    platform: string;
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
                packages: packagesUniq,
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

export interface PlatformProfileProps {
    type: PlatformType;
    profile?: any;
    onFinish?: (profile: any) => void;
}

export interface IPlatformProfileHandler {
    value: () => string;
}

export class PlatformProfileHandler {
    private inner: IPlatformProfileHandler;

    constructor(handler: IPlatformProfileHandler) {
        this.inner = handler;
    }

    public update(handler: IPlatformProfileHandler) {
        this.inner = handler;
    }

    public value() {
        return this.inner.value();
    }
}

export function PlatformProfile(props: PlatformProfileProps) {
    const { type, profile, onFinish } = props;
    const [isDefault, setIsDefault] = useState(profile === undefined);
    const [error, setError] = useState<any>(null);

    const schema = platformSchema(type);
    const handlerRef = useRef<PlatformProfileHandler>(
        new PlatformProfileHandler({
            value: () => "",
        })
    );

    const handleWillMount = (monaco: Monaco) => {
        monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            validate: true,
            schemas: [
                {
                    uri: type,
                    schema: schema,
                },
            ],
        });
    };

    const handleDidMount = (editor: EditorApi.editor.IStandaloneCodeEditor, monaco: Monaco) => {
        const handler: PlatformProfileHandler = handlerRef.current;
        handler.update({
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

    const onSwitch = () => {
        setIsDefault(!isDefault);
        setError(null);
    };

    const mode = () => {
        if (isDefault) {
            return "Default";
        } else {
            return "Custom";
        }
    };

    const onConfirm = () => {
        if (isDefault) {
            if (onFinish !== undefined) {
                onFinish(undefined);
            }
            return;
        }
        const payload = handlerRef.current.value();
        try {
            const json = JSON.parse(payload);
            if (onFinish !== undefined) {
                onFinish(json);
            }
            setError(null);
        } catch (reason) {
            setError(reason);
            console.error(reason);
        }
    };

    const profileValue: string = profile === undefined ? "" : JSON.stringify(profile, null, 2);

    return (
        <Paper
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Stack
                direction="column"
                justifyContent="flex-start"
                alignItems="stretch"
                spacing={2}
                sx={{
                    width: "100%",
                }}
            >
                {error !== null && (
                    <Alert severity="error">{"Fail to parse your input into json format."}</Alert>
                )}
                <Paper>
                    <Stack
                        direction="row"
                        justifyContent="space-between"
                        alignItems="center"
                        spacing={2}
                    >
                        <div
                            style={{
                                display: "flex",
                                flexDirection: "row",
                                justifyContent: "flex-start",
                                alignContent: "center",
                                alignItems: "center",
                            }}
                        >
                            <Switch onClick={onSwitch} />
                            <Typography>{mode()}</Typography>
                        </div>
                        <Button variant="contained" color="primary" onClick={onConfirm}>
                            {"Confirm"}
                        </Button>
                    </Stack>
                </Paper>
                {!isDefault && (
                    <Paper>
                        <Editor
                            height={"60vh"}
                            defaultLanguage={"json"}
                            defaultValue={profileValue}
                            theme={"vs-dark"}
                            beforeMount={handleWillMount}
                            onMount={handleDidMount}
                        />
                    </Paper>
                )}
            </Stack>
        </Paper>
    );
}

export interface BackendArgsProfileProps {
    profile?: string[];
    onFinish?: (args: string[]) => void;
}

export function BackendArgsProfile(props: BackendArgsProfileProps) {
    const { profile, onFinish } = props;
    const schema: RJSFSchema = {
        title: "Backend Arguments",
        type: "array",
        items: {
            type: "string",
        },
    };

    const onSubmit = (data: IChangeEvent<any, RJSFSchema, any>) => {
        const newProfile: string[] = data.formData;
        if (onFinish !== undefined) {
            onFinish(newProfile);
        }
    };

    return (
        <Paper
            square
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Form
                formData={profile === undefined ? [] : profile}
                schema={schema}
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

export interface ProjectProfileFinishProps {
    isCreate: boolean;
    onFinish?: () => void;
}

export function ProjectProfileFinish(props: ProjectProfileFinishProps) {
    const { isCreate, onFinish } = props;

    return (
        <Paper
            square
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
            }}
        >
            <Container
                sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignContent: "center",
                    justifyContent: "center",
                    alignItems: "center",
                    height: "80%",
                }}
            >
                <Stack direction="column" justifyContent="center" alignItems="stretch" spacing={2}>
                    <Alert
                        variant="outlined"
                        severity="info"
                        icon={<VscInfo fontSize={"2em"} />}
                        sx={{
                            fontSize: "2em",
                        }}
                    >
                        {"Profile Will Finish"}
                    </Alert>
                    {isCreate && <Button variant={"contained"}>{"Create"}</Button>}
                    {!isCreate && <Button variant={"contained"}>{"Update"}</Button>}
                </Stack>
            </Container>
        </Paper>
    );
}

export interface ProjectProfileProps {
    open: boolean;
    isCreate: boolean;
}

export interface ProjectProfileHandler {
    open: () => void;
    close: () => void;
}

const STEP_SIZE = 5;

export const ProjectProfile = forwardRef(
    (props: ProjectProfileProps, ref: ForwardedRef<ProjectProfileHandler>) => {
        const { open, isCreate } = props;
        const [isOpen, setOpen] = useState(open);
        const [activeStep, setActiveStep] = useState(0);
        const [basicProfile, setBasicProfile] = useState<ProjectBasicProfileValue>();
        const [pkgProfile, setPkgProfile] = useState<PkgProfileValue>();
        const [platformProfile, setPlatformProfile] = useState<any>();
        const [backendArgsProfile, setBackendArgsProfile] = useState<string[]>();

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

        const onPlatformProfileFinish = (newProfile: any) => {
            setPlatformProfile(newProfile);
            handleNextStep();
        };

        const onBackendArgsProfileFinish = (newProfile: string[]) => {
            setBackendArgsProfile(newProfile);
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
                    <Container>
                        <Stepper
                            activeStep={activeStep}
                            sx={{
                                height: "10vh",
                            }}
                        >
                            <Step key={"Basic Profile."} completed={false}>
                                <StepLabel>{"Basic Profile."}</StepLabel>
                            </Step>
                            <Step key={"Package select."} completed={false}>
                                <StepLabel>{"Package select."}</StepLabel>
                            </Step>
                            <Step key={"Platform profile."} completed={false}>
                                <StepLabel>{"Platform profile."}</StepLabel>
                            </Step>
                            <Step key={"Backend arguments profile."} completed={false}>
                                <StepLabel>{"Platform profile."}</StepLabel>
                            </Step>
                            <Step key={"Profile Finish."} completed={false}>
                                <StepLabel>{"Profile Finish."}</StepLabel>
                            </Step>
                        </Stepper>
                    </Container>

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
                        <PlatformProfile
                            type={
                                basicProfile === undefined
                                    ? PlatformType.SPARK_STANDALONE
                                    : platformType(basicProfile.platform)
                            }
                            profile={basicProfile === undefined ? undefined : platformProfile}
                            onFinish={onPlatformProfileFinish}
                        />
                    )}
                    {activeStep === 3 && (
                        <BackendArgsProfile
                            profile={backendArgsProfile}
                            onFinish={onBackendArgsProfileFinish}
                        />
                    )}
                    {activeStep === 4 && <ProjectProfileFinish isCreate={isCreate} />}

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
                </div>
            </ResizeBackdrop>
        );
    }
);
