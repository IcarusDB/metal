import './Project.css';
import {
    ApiTwoTone,
    CheckCircleTwoTone,
    QuestionCircleTwoTone,
    StopTwoTone,
    ThunderboltTwoTone,
    UserOutlined,
    WarningFilled,
    EyeTwoTone,
    EditTwoTone
} from '@ant-design/icons';
import {useAppSelector} from "../../app/hooks";
import {tokenSelector} from "../user/userSlice";
import {Card, Descriptions, List, message, Skeleton, Popover, Row, Col, Button, Divider} from "antd";
import {useEffect, useState} from "react";
import {BackendState, BackendStatus, Deploy, Project} from "../../model/Project";
import {getAllProjectOfUser} from "./ProjectApi";

function backendStatusTip(backendStatus: BackendStatus) {
    const upTime = backendStatus.upTime === undefined ? <></> : (
        <Descriptions.Item label={'Up Time'}>{backendStatus.upTime}</Descriptions.Item>
    )

    const downTime = backendStatus.downTime === undefined ? <></> : (
        <Descriptions.Item label={'Down Time'}>{backendStatus.downTime}</Descriptions.Item>
    )

    const failureTime = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <Descriptions.Item label={'Failure Time'}>{backendStatus.failureTime}</Descriptions.Item>
    )

    const failureMsg = backendStatus.failureTime === undefined ||
    backendStatus.current !== BackendState.FAILURE ? <></> : (
        <Descriptions.Item label={'Failure Message'}>{backendStatus.failureMsg}</Descriptions.Item>
    )

    return (
        <Descriptions>
            <Descriptions.Item label={'current'}>{backendStatus.current}</Descriptions.Item>
            {upTime}
            {downTime}
            {failureTime}
            {failureMsg}
        </Descriptions>
    )
}

function backendStatus(deploy: Deploy) {
    if (deploy.backend === undefined || deploy.backend.status === undefined) {
        return (
            <Popover content={'No deployment is set.'}>
                <StopTwoTone/>
            </Popover>
        )
    }

    switch (deploy.backend.status.current) {
        case BackendState.UN_DEPLOY: {
            return (
                <Popover content={backendStatusTip(deploy.backend.status)}>
                    <ApiTwoTone/>
                </Popover>
            )
        }
            ;
        case BackendState.UP: {
            return (
                <Popover content={backendStatusTip(deploy.backend.status)}>
                    <ThunderboltTwoTone/>
                </Popover>
            )
        }
            ;
        case BackendState.DOWN: {
            return (
                <Popover content={backendStatusTip(deploy.backend.status)}>
                    <CheckCircleTwoTone/>
                </Popover>
            )
        }
            ;
        case BackendState.FAILURE: {
            return (
                <Popover content={backendStatusTip(deploy.backend.status)}>
                    <WarningFilled/>
                </Popover>
            )
        }
            ;
        default: {
            return (
                <Popover content={'Unknown'}>
                    <QuestionCircleTwoTone/>
                </Popover>
            )
        }
    }

}

function projectItemBar(item: Project) {
    return (
        <Row justify={'space-between'}>
            <Col><span>{item.name}</span></Col>
            <Col>
                <Divider type={"vertical"}/>
                <Button.Group>
                    <Button icon={<EyeTwoTone />}></Button>
                    <Button icon={<EditTwoTone />}></Button>
                </Button.Group>
            </Col>
        </Row>
    )
}

export function ProjectItem(props: { item: Project, index: number }) {
    const {item, index} = props;
    return (
        <List.Item
            key={item.id}
        >
            <Card title={projectItemBar(item)} hoverable={true}>
                <Descriptions column={1} bordered={false}>
                    <Descriptions.Item label={<UserOutlined/>}>{item.user.username}</Descriptions.Item>
                    <Descriptions.Item label={'Status'}>{backendStatus(item.deploy)}</Descriptions.Item>
                </Descriptions>
            </Card>
        </List.Item>
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
                message.error("Fail to get projects.")
            })
        }
    }, [token])

    if (token == null) {
        return (
            <Skeleton/>
        )
    }

    console.log(projects)

    return (
        <div className={'panel'}>
            <List
                pagination={{pageSize: 10}}
                dataSource={projects}
                renderItem={(item: Project, index: number) => {
                    return ProjectItem({item: item, index: index})
                }}
            />
        </div>
    )


}

