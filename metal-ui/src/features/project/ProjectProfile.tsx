import {
    Alert,
    Button,
    Container,
    Grid,
    IconButton,
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
    useCallback,
    useEffect,
    useImperativeHandle,
    useMemo,
    useRef,
    useState,
} from "react";
import { VscArrowLeft, VscClose, VscError, VscInfo, VscWarning } from "react-icons/vsc";
import { platformSchema, platformType, PlatformType, Project } from "../../model/Project";
import { useAsync } from "../../api/Hooks";
import { MetalPkg } from "../../model/MetalPkg";
import _ from "lodash";
import { getAllMetalPkgsOfUserAccess } from "../designer/explorer/MetalPkgApi";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { State } from "../../api/State";
import { IChangeEvent } from "@rjsf/core";
import Editor, { Monaco } from "@monaco-editor/react";
import * as EditorApi from "monaco-editor/esm/vs/editor/editor.api";
import { createProject, ProjectParams } from "./ProjectApi";

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
    const { run, status, result, error } = useAsync<MetalPkg[]>();

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

    useEffect(() => {
        if (token !== null) {
            run(getAllMetalPkgsOfUserAccess(token));
        }
    }, [run, token]);

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
                    {progress}
                    {tbl}
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
    const [isDefault, setIsDefault] = useState(profile === undefined || profile === null);
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

    const profileValue: string =
        profile === undefined || profile === null ? "" : JSON.stringify(profile, null, 2);

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
    profile: ProjectProfileValue;
    onFinish?: (projectId: string) => void;
}

export function ProjectProfileFinish(props: ProjectProfileFinishProps) {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const { isCreate, profile, onFinish } = props;
    const { basic, pkgs, platform, backendArgs } = profile;
    const [warnTip, setWarnTip] = useState<string>();
    const { run, status, error, result } = useAsync<string>();

    const check: () => [boolean, string | undefined] = useCallback(() => {
        if (basic === null || basic.name === "") {
            return [false, "The project basic profile is not configured."];
        }

        if (pkgs === null || pkgs.packages === null || pkgs.packages.length === 0) {
            return [false, "The project packages is not configured."];
        }

        if (platform !== null) {
            if (platform[basic.platform] === undefined) {
                return [false, `The project platform[${basic.platform}] is not configured.`];
            }
        }
        return [true, undefined];
    }, [basic, pkgs, platform]);

    const isPending = () => status === State.pending;
    const isSuccess = () => status === State.success;
    const isFailure = () => status === State.failure;

    const progress = isPending() ? (
        <LinearProgress />
    ) : (
        <LinearProgress variant="determinate" value={0} />
    );

    const onCreate = () => {
        if (token === null) {
            setWarnTip("User is not authorized");
            return;
        }
        const [isChecked, msg] = check();
        if (!isChecked) {
            setWarnTip(msg);
        } else {
            const params: ProjectParams = {
                name: profile.basic === null ? undefined : profile.basic.name,
                pkgs:
                    profile.pkgs === null
                        ? undefined
                        : profile.pkgs.packages.map(
                              (pkg) => `${pkg.groupId}:${pkg.artifactId}:${pkg.version}`
                          ),
                platform:
                    profile.platform === null || profile.platform === undefined
                        ? undefined
                        : profile.platform,
                backendArgs: profile.backendArgs === null ? undefined : profile.backendArgs,
            };
            run(createProject(token, params));
        }
    };

    const onOpenProject = () => {
        if (isSuccess()) {
            if (onFinish !== undefined && result !== null) {
                onFinish(result);
            }
        }
    };

    const onUpdate = () => {
        if (token === null) {
            setWarnTip("User is not authorized");
            return;
        }
        const [isChecked, msg] = check();
        if (!isChecked) {
            setWarnTip(msg);
        } else {
            // run(createProject(token, profile));
        }
    };

    useEffect(() => {
        const [isChecked, msg] = check();
        if (!isChecked) {
            setWarnTip(msg);
        }
    }, [check]);

    return (
        <Paper
            square
            sx={{
                boxSizing: "border-box",
                margin: "0px",
                width: "100%",
                height: "100%",
                position: "relative",
            }}
        >
            {isPending() && progress}
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
                    {warnTip !== undefined && (
                        <Alert
                            variant="outlined"
                            severity="warning"
                            icon={<VscWarning fontSize={"2em"} />}
                            sx={{
                                fontSize: "2em",
                            }}
                        >
                            {warnTip}
                        </Alert>
                    )}
                    {isFailure() && (
                        <Alert
                            variant="outlined"
                            severity="error"
                            icon={<VscError fontSize={"2em"} />}
                            sx={{
                                fontSize: "2em",
                            }}
                        >
                            {"Fail to request server."}
                        </Alert>
                    )}

                    <Alert
                        variant="outlined"
                        severity="info"
                        icon={<VscInfo fontSize={"2em"} />}
                        sx={{
                            fontSize: "2em",
                        }}
                    >
                        {!isSuccess() ? "Profile Will Finish" : "Profile is Finished."}
                    </Alert>
                    {isCreate && (
                        <Button variant={"contained"} onClick={onCreate}>
                            {"Create"}
                        </Button>
                    )}
                    {!isCreate && <Button variant={"contained"}>{"Update"}</Button>}
                    {isCreate && isSuccess() && (
                        <Button variant={"contained"} onClick={onOpenProject}>
                            {"Open Project"}
                        </Button>
                    )}
                </Stack>
            </Container>
            <ResizeBackdrop open={isPending()} />
        </Paper>
    );
}

export interface ProjectProfileProps {
    open: boolean;
    isCreate: boolean;
    project?: Project;
    onFinish?: (projectId: string) => void;
}

export interface ProjectProfileHandler {
    open: () => void;
    close: () => void;
}

export interface ProjectProfileValue {
    basic: ProjectBasicProfileValue | null;
    pkgs: PkgProfileValue | null;
    platform: any;
    backendArgs: string[] | null;
}

const STEP_SIZE = 5;

function extractBasicProfile(project: Project | undefined): ProjectBasicProfileValue | null {
    if (project === undefined || project === null) {
        return null;
    }
    const platforms = _.keys(project.deploy.platform);
    if (platforms.length === 0) {
        return {
            name: project.name,
            platform: PlatformType.SPARK_STANDALONE,
        };
    }

    return {
        name: project.name,
        platform: platformType(platforms[0]),
    };
}

function extractPkgProfile(project: Project | undefined): PkgProfileValue | null {
    if (project === undefined || project === null) {
        return null;
    }
    return {
        packages: project.deploy.pkgs
            .filter((pkg) => {
                return pkg.split(":").length === 3;
            })
            .map((pkg) => {
                const sub = pkg.split(":");
                return {
                    id: pkg,
                    groupId: sub[0],
                    artifactId: sub[1],
                    version: sub[2],
                    scope: "PRIVATE",
                };
            }),
    };
}

function extractPlatformProfile(type: PlatformType, project: Project | undefined): any {
    if (project === undefined || project === null) {
        return null;
    }
    if (_.hasIn(project.deploy.platform, type)) {
        return project.deploy.platform[type];
    } else {
        return null;
    }
}

function extractBackendArgumentsProfile(project: Project | undefined): string[] | null {
    if (project === undefined || project === null) {
        return null;
    }
    return project.deploy.backend.args;
}

export const ProjectProfile = forwardRef(
    (props: ProjectProfileProps, ref: ForwardedRef<ProjectProfileHandler>) => {
        const { open, isCreate, project, onFinish } = props;
        const [isOpen, setOpen] = useState(open);
        const [activeStep, setActiveStep] = useState(0);

        const [basicProfile, setBasicProfile] = useState<ProjectBasicProfileValue | null>(() =>
            extractBasicProfile(project)
        );
        const [pkgProfile, setPkgProfile] = useState<PkgProfileValue | null>(() =>
            extractPkgProfile(project)
        );
        const [platformProfile, setPlatformProfile] = useState<any>(() =>
            extractPlatformProfile(
                basicProfile === null
                    ? PlatformType.SPARK_STANDALONE
                    : platformType(basicProfile.platform),
                project
            )
        );
        const [backendArgsProfile, setBackendArgsProfile] = useState<string[] | null>(() =>
            extractBackendArgumentsProfile(project)
        );

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

        const platformWithType = () => {
            if (platformProfile === null || platformProfile === undefined) {
                return null;
            }
            if (basicProfile === null) {
                return null;
            }
            const withType = `{"${basicProfile.platform}": ${JSON.stringify(platformProfile)}}`;

            return JSON.parse(withType);
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
                    {!isCreate && (
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
                    )}

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
                            profile={basicProfile === null ? undefined : basicProfile}
                            onFinish={onBasicProfileFinish}
                        />
                    )}
                    {activeStep === 1 && (
                        <PkgSelector
                            profile={pkgProfile === null ? undefined : pkgProfile}
                            onFinish={onPkgProfileFinish}
                        />
                    )}
                    {activeStep === 2 && (
                        <PlatformProfile
                            type={
                                basicProfile === null
                                    ? PlatformType.SPARK_STANDALONE
                                    : platformType(basicProfile.platform)
                            }
                            profile={basicProfile === null ? undefined : platformProfile}
                            onFinish={onPlatformProfileFinish}
                        />
                    )}
                    {activeStep === 3 && (
                        <BackendArgsProfile
                            profile={backendArgsProfile === null ? [] : backendArgsProfile}
                            onFinish={onBackendArgsProfileFinish}
                        />
                    )}
                    {activeStep === 4 && (
                        <ProjectProfileFinish
                            isCreate={isCreate}
                            profile={{
                                basic: basicProfile,
                                pkgs: pkgProfile,
                                platform: platformWithType(),
                                backendArgs: backendArgsProfile,
                            }}
                            onFinish={onFinish}
                        />
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
                </div>
            </ResizeBackdrop>
        );
    }
);