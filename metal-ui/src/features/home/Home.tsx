import { Accordion, AccordionDetails, AccordionSummary, Button, Card, CardContent, Divider, List, ListItem, Skeleton, Stack, Typography } from "@mui/material";
import { VscChevronDown, VscExtensions, VscFolderOpened, VscMerge, VscNewFolder } from "react-icons/vsc";
import {MdFlashOn, MdSummarize, MdWarning} from "react-icons/md";
import { useAppSelector } from "../../app/hooks";
import { tokenSelector } from "../user/userSlice";
import { FaStop } from "react-icons/fa";
import { ImDownload, ImUpload } from "react-icons/im";
import { AiOutlineFunction } from "react-icons/ai";

export function Home() {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    if (token === null) {
        return <Skeleton />
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
            <List
                dense
                disablePadding={true}
            >
                <ListItem>
                <Button startIcon={<VscNewFolder />}>New Project</Button>
                </ListItem>
                <ListItem>
                <Button startIcon={<VscFolderOpened />}>Open Project</Button>
                </ListItem>
            </List>
            <ProjectSummary />
            <MetalRepoSummary />
        </div>
    )
}

const ICON_SIZE = "4vw";
function ProjectSummary() {
    return (
        <Accordion
            defaultExpanded={true}
        >
            <AccordionSummary 
                expandIcon={<VscChevronDown size={"2em"}/>}
            >
                <Typography variant="h5">
                    Project
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Divider orientation="horizontal" flexItem/>
                <Stack
                    sx={{
                        boxSizing: "border-box",
                        paddingTop: "1vh",
                    }}
                    direction="row"
                    justifyContent="flex-start"
                    alignItems="center"
                    spacing={2}
                >
                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent:"space-between",
                            }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Total
                            </Typography>
                            <Typography variant="h3" color={"text.secondary"}>
                                30
                            </Typography>
                            </div>
                            <MdSummarize size={ICON_SIZE} color={"gray"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent:"space-between",
                            }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Down
                            </Typography>
                            <Typography variant="h3" color={"darkblue"}>
                                10
                            </Typography>
                            </div>
                            <FaStop size={ICON_SIZE} color={"darkblue"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent:"space-between",
                        }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Up
                            </Typography>
                            <Typography variant="h3" color={"yellowgreen"}>
                                10
                            </Typography>
                            </div>
                            <MdFlashOn size={ICON_SIZE} color={"yellowgreen"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent:"space-between",
                        }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Failure
                            </Typography>
                            <Typography variant="h3" color={"red"}>
                                10
                            </Typography>
                            </div>
                            <MdWarning size={ICON_SIZE} color={"red"}/>
                        </CardContent>
                    </Card>

                </Stack>
            </AccordionDetails>
        </Accordion>
    )
}

function MetalRepoSummary() {
    return (
        <Accordion
            defaultExpanded={true}
        >
            <AccordionSummary 
                expandIcon={<VscChevronDown size={"2em"}/>}
            >
                <Typography variant="h5">
                    Metal Repository
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Divider orientation="horizontal" flexItem/>
                <Stack
                    sx={{
                        boxSizing: "border-box",
                        paddingTop: "1vh",
                    }}
                    direction="row"
                    justifyContent="flex-start"
                    alignItems="center"
                    spacing={2}
                >
                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent:"space-between",
                            }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Total
                            </Typography>
                            <Typography variant="h3" color={"text.secondary"}>
                                23
                            </Typography>
                            </div>
                            <MdSummarize size={ICON_SIZE} color={"gray"}/>
                        </CardContent>
                    </Card>
                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent:"space-between",
                            }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Setup
                            </Typography>
                            <Typography variant="h3" color={"text.secondary"}>
                                3
                            </Typography>
                            </div>
                            <VscExtensions size={ICON_SIZE} color={"gray"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent:"space-between",
                            }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Source
                            </Typography>
                            <Typography variant="h3" color={"darkblue"}>
                                2
                            </Typography>
                            </div>
                            <ImUpload size={ICON_SIZE} color={"darkblue"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent:"space-between",
                        }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Sink
                            </Typography>
                            <Typography variant="h3" color={"yellowgreen"}>
                                10
                            </Typography>
                            </div>
                            <ImDownload size={ICON_SIZE} color={"yellowgreen"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent:"space-between",
                        }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Mapper
                            </Typography>
                            <Typography variant="h3" color={"cyan"}>
                                4
                            </Typography>
                            </div>
                            <AiOutlineFunction size={ICON_SIZE} color={"cyan"}/>
                        </CardContent>
                    </Card>

                    <Card
                        sx={{
                            minWidth: "15vw",
                        }}
                    >
                        <CardContent
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent:"space-between",
                        }}
                        >
                            <div>
                            <Typography variant="h6" color={"text.secondary"}>
                                Fusion
                            </Typography>
                            <Typography variant="h3" color={"orange"}>
                                4
                            </Typography>
                            </div>
                            <VscMerge size={ICON_SIZE} color={"orange"}/>
                        </CardContent>
                    </Card>

                </Stack>
            </AccordionDetails>
        </Accordion>
    )
}