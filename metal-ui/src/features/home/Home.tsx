import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Button,
    Card,
    CardContent,
    Divider,
    List,
    ListItem,
    Skeleton,
    Typography,
} from "@mui/material";
import {
    VscChevronDown,
    VscExtensions,
    VscFolderOpened,
    VscMerge,
    VscNewFolder,
} from "react-icons/vsc";
import { MdFlashOn, MdSummarize, MdWarning } from "react-icons/md";
import { BsHourglassSplit } from "react-icons/bs";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { FaStop } from "react-icons/fa";
import { ImDownload, ImUpload } from "react-icons/im";
import { AiOutlineFunction } from "react-icons/ai";
import { useAsync } from "../../api/Hooks";
import { useEffect, useRef } from "react";
import { BackendState, Project } from "../../model/Project";
import { getAllProjectOfUser } from "../../api/ProjectApi";
import { MetalPkg } from "../../model/MetalPkg";
import { metalType, MetalTypes } from "../../model/Metal";
import { getAllMetalPkgsOfUserAccess } from "../../api/MetalPkgApi";
import { MainHandler } from "../main/Main";

export interface HomeProps {
    mainHandler: MainHandler;
}

export function Home(props: HomeProps) {
    const { mainHandler } = props;
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const starterCounter = useRef(0);

    const onNewProject = () => {
        mainHandler.openProjectStarter({
            id: `starter[${starterCounter.current++}]`,
            mainHandler: mainHandler,
        });
    };

    const onOpenProject = () => {
        mainHandler.select("projects_tab");
    };

    const onOpenMetalRepo = () => {
        mainHandler.openMetalRepo({});
    };

    if (token === null) {
        return <Skeleton />;
    }

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
                Starter
            </Typography>
            <List dense disablePadding={true}>
                <ListItem key="newProject" >
                    <Button startIcon={<VscNewFolder />} onClick={onNewProject}>
                        New Project
                    </Button>
                </ListItem>
                <ListItem key="openProject">
                    <Button startIcon={<VscFolderOpened />} onClick={onOpenProject}>
                        Open Project
                    </Button>
                </ListItem>
                <ListItem key="metalRepository">
                    <Button startIcon={<VscExtensions />} onClick={onOpenMetalRepo}>
                        Metal Repository
                    </Button>
                </ListItem>
            </List>
            <ProjectSummary token={token} />
            <MetalRepoSummary token={token} />
        </div>
    );
}

const ICON_SIZE = "4vw";
const CARD_H_PAD = "2vw";
const CARD_V_PAD = "2vh";

interface ProjectSummaryProps {
    token: string | null;
}

interface ProjectSummaryResult {
    total: number;
    created: number;
    up: number;
    down: number;
    failure: number;
}

function useProjectSummary(token: string | null): ProjectSummaryResult {
    const [run, status, result, error] = useAsync<Project[]>();

    useEffect(() => {
        if (token !== null) {
            run(getAllProjectOfUser(token));
        }
    }, [run, token]);

    if (result === null) {
        return {
            total: -1,
            created: -1,
            up: -1,
            down: -1,
            failure: -1,
        };
    }

    const created = result.filter(
        (proj: Project) => proj.deploy.backend.status.current === BackendState.CREATED
    ).length;
    const down = result.filter(
        (proj: Project) => proj.deploy.backend.status.current === BackendState.DOWN
    ).length;
    const up = result.filter(
        (proj: Project) => proj.deploy.backend.status.current === BackendState.UP
    ).length;
    const failure = result.filter(
        (proj: Project) => proj.deploy.backend.status.current === BackendState.FAILURE
    ).length;
    return {
        total: created + down + up + failure,
        created: created,
        down: down,
        up: up,
        failure: failure,
    };
}

function ProjectSummary(props: ProjectSummaryProps) {
    const { token } = props;
    const { total, created, down, up, failure } = useProjectSummary(token);

    return (
        <Accordion defaultExpanded={true}>
            <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
                <Typography variant="h5">Project</Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Divider orientation="horizontal" flexItem />
                <div
                    style={{
                        boxSizing: "border-box",
                        paddingTop: "1vh",
                        flexWrap: "wrap",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "flex-start",
                    }}
                >
                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Total
                                </Typography>
                                <Typography variant="h3" color={"text.secondary"}>
                                    {total}
                                </Typography>
                            </div>
                            <MdSummarize size={ICON_SIZE} color={"gray"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Created
                                </Typography>
                                <Typography variant="h3" color={"cyan"}>
                                    {created}
                                </Typography>
                            </div>
                            <BsHourglassSplit size={ICON_SIZE} color={"cyan"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Down
                                </Typography>
                                <Typography variant="h3" color={"darkblue"}>
                                    {down}
                                </Typography>
                            </div>
                            <FaStop size={ICON_SIZE} color={"darkblue"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Up
                                </Typography>
                                <Typography variant="h3" color={"yellowgreen"}>
                                    {up}
                                </Typography>
                            </div>
                            <MdFlashOn size={ICON_SIZE} color={"yellowgreen"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Failure
                                </Typography>
                                <Typography variant="h3" color={"red"}>
                                    {failure}
                                </Typography>
                            </div>
                            <MdWarning size={ICON_SIZE} color={"red"} />
                        </CardContent>
                    </Card>
                </div>
            </AccordionDetails>
        </Accordion>
    );
}

interface MetalRepoSummaryProps {
    token: string | null;
}

interface MetalRepoSummaryResult {
    total: number;
    setup: number;
    source: number;
    sink: number;
    mapper: number;
    fusion: number;
}

function useMetalRepoSummary(token: string | null): MetalRepoSummaryResult {
    const [run, status, result, error] = useAsync<MetalPkg[]>();

    useEffect(() => {
        if (token !== null) {
            run(getAllMetalPkgsOfUserAccess(token));
        }
    }, [run, token]);

    if (result === null) {
        return {
            total: -1,
            setup: -1,
            source: -1,
            sink: -1,
            mapper: -1,
            fusion: -1,
        };
    }

    const setup = result.filter((pkg: MetalPkg) => metalType(pkg.type) === MetalTypes.SETUP).length;
    const source = result.filter(
        (pkg: MetalPkg) => metalType(pkg.type) === MetalTypes.SOURCE
    ).length;
    const sink = result.filter((pkg: MetalPkg) => metalType(pkg.type) === MetalTypes.SINK).length;
    const mapper = result.filter(
        (pkg: MetalPkg) => metalType(pkg.type) === MetalTypes.MAPPER
    ).length;
    const fusion = result.filter(
        (pkg: MetalPkg) => metalType(pkg.type) === MetalTypes.FUSION
    ).length;
    return {
        total: setup + source + sink + mapper + fusion,
        setup: setup,
        source: source,
        sink: sink,
        mapper: mapper,
        fusion: fusion,
    };
}

function MetalRepoSummary(props: MetalRepoSummaryProps) {
    const { token } = props;
    const { total, setup, source, sink, mapper, fusion } = useMetalRepoSummary(token);

    return (
        <Accordion defaultExpanded={true}>
            <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
                <Typography variant="h5">Metal Repository</Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Divider orientation="horizontal" flexItem />
                <div
                    style={{
                        boxSizing: "border-box",
                        paddingTop: "1vh",
                        flexWrap: "wrap",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "flex-start",
                    }}
                >
                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Total
                                </Typography>
                                <Typography variant="h3" color={"text.secondary"}>
                                    {total}
                                </Typography>
                            </div>
                            <MdSummarize size={ICON_SIZE} color={"gray"} />
                        </CardContent>
                    </Card>
                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Setup
                                </Typography>
                                <Typography variant="h3" color={"text.secondary"}>
                                    {setup}
                                </Typography>
                            </div>
                            <VscExtensions size={ICON_SIZE} color={"gray"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Source
                                </Typography>
                                <Typography variant="h3" color={"darkblue"}>
                                    {source}
                                </Typography>
                            </div>
                            <ImUpload size={ICON_SIZE} color={"darkblue"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Sink
                                </Typography>
                                <Typography variant="h3" color={"yellowgreen"}>
                                    {sink}
                                </Typography>
                            </div>
                            <ImDownload size={ICON_SIZE} color={"yellowgreen"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Mapper
                                </Typography>
                                <Typography variant="h3" color={"cyan"}>
                                    {mapper}
                                </Typography>
                            </div>
                            <AiOutlineFunction size={ICON_SIZE} color={"cyan"} />
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                            boxSizing: "border-box",
                            marginTop: CARD_V_PAD,
                            marginBottom: CARD_V_PAD,
                            marginLeft: CARD_H_PAD,
                            marignRight: CARD_H_PAD,
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <div>
                                <Typography variant="h6" color={"text.secondary"}>
                                    Fusion
                                </Typography>
                                <Typography variant="h3" color={"orange"}>
                                    {fusion}
                                </Typography>
                            </div>
                            <VscMerge size={ICON_SIZE} color={"orange"} />
                        </CardContent>
                    </Card>
                </div>
            </AccordionDetails>
        </Accordion>
    );
}
