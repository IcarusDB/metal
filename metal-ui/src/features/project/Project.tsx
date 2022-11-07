import './Project.css';
import {useAppSelector} from "../../app/hooks";
import {tokenSelector} from "../user/userSlice";
import {
    Card,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Tooltip,
    ListItemButton,
    CardHeader,
    CardContent,
    Snackbar
} from "@mui/material";
import {AiOutlineStop, AiOutlineApi, AiFillThunderbolt, AiOutlineWarning, AiOutlineQuestionCircle} from "react-icons/ai";
import {HiStop} from "react-icons/hi";
import {useEffect, useState} from "react";
import {BackendState, BackendStatus, Deploy, Project} from "../../model/Project";
import {getAllProjectOfUser} from "./ProjectApi";


function backendStatusTip(backendStatus: BackendStatus) {
    const upTime = backendStatus.upTime === undefined ? <></> : (
        <ListItem>
            <ListItemText>{'Up Time'}</ListItemText>
            <ListItemText>{backendStatus.upTime}</ListItemText>
        </ListItem>
    )

    const downTime = backendStatus.downTime === undefined ? <></> : (
        <ListItem>
            <ListItemText>{'Down Time'}</ListItemText>
            <ListItemText>{backendStatus.downTime}</ListItemText>
        </ListItem>
    )

    const failureTime = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <ListItem>
            <ListItemText>{'Failure Time'}</ListItemText>
            <ListItemText>{backendStatus.failureTime}</ListItemText>
        </ListItem>
    )

    const failureMsg = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <ListItem>
            <ListItemText>{'Failure Message'}</ListItemText>
            <ListItemText>{backendStatus.failureMsg}</ListItemText>
        </ListItem>
    )

    return (
        <List>
            <ListItem>
                <ListItemText>{'current'}</ListItemText>
                <ListItem>{backendStatus.current}</ListItem>
            </ListItem>
            {upTime}
            {downTime}
            {failureTime}
            {failureMsg}
        </List>
    )
}

function backendStatus(deploy: Deploy) {
    if (deploy.backend === undefined || deploy.backend.status === undefined) {
        return (
            <Tooltip title={'No deployment is set.'}>
                <AiOutlineStop/>
            </Tooltip>
        )
    }

    switch (deploy.backend.status.current) {
        case BackendState.UN_DEPLOY: {
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <AiOutlineApi/>
                </Tooltip>
            )
        }
            ;
        case BackendState.UP: {
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <AiFillThunderbolt/>
                </Tooltip>
            )
        }
            ;
        case BackendState.DOWN: {
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <HiStop/>
                </Tooltip>
            )
        }
            ;
        case BackendState.FAILURE: {
            return (
                <Tooltip title={backendStatusTip(deploy.backend.status)}>
                    <AiOutlineWarning/>
                </Tooltip>
            )
        }
            ;
        default: {
            return (
                <Tooltip title={'Unknown'}>
                    <AiOutlineQuestionCircle/>
                </Tooltip>
            )
        }
    }

}

function projectItemBar(item: Project) {
    return (
        <ListItem>
            <ListItemText>{item.name}</ListItemText>
            <ListItemButton></ListItemButton>
            <ListItemButton></ListItemButton>
        </ListItem>
    )
}

export function ProjectItem(props: { item: Project, index: number }) {
    const {item, index} = props;
    return (
        <ListItem>
            <Card>
                <CardHeader title={item.name}/>
                <CardContent>
                    <List>
                        <ListItem>
                            <ListItemText>{'User Name'}</ListItemText>
                            <ListItemText>{item.user.username}</ListItemText>
                        </ListItem>
                        <ListItem>
                            <ListItemText>{'Status'}</ListItemText>
                            <ListItemText>{backendStatus(item.deploy)}</ListItemText>
                        </ListItem>
                    </List>
                </CardContent>
            </Card>
        </ListItem>
    )
}

export function ProjectList() {
    const token: string | null = useAppSelector(state => {
        return tokenSelector(state)
    })
    const [projects, setProjects] = useState<Project[]>([])

    useEffect(() => {
        if (token != null) {
            getAllProjectOfUser(token).then((_projects: Project[]) => {
                setProjects(_projects)
            }, reason => {
                console.error(reason)
                // Snackbar.
                // message.error("Fail to get projects.")
            })
        }
    }, [token])

    return (
        <div className={'panel'}>
            <List>
                {projects.map((item: Project, index: number) => {
                    return ProjectItem({item: item, index: index})
                })}
            </List>
        </div>
    )


}

