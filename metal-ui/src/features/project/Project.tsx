
import {useAppSelector} from "../../app/hooks";
import {tokenSelector} from "../user/userSlice";
import {List, Tag, Skeleton, message} from "antd";
import {useEffect, useState} from "react";
import {Project} from "../../model/Project";
import {getAllProjectOfUser} from "./ProjectApi";

export function ProjectItem(props: {item: Project, index: number}) {
    const {item, index} = props;
    return (
        <List.Item
            key={item.id}
        >
            <Tag color="#f50">{item.name}</Tag>
            <Tag color="#f50">{item.user.username}</Tag>
        </List.Item>
    )
}

export function ProjectList() {
    const token: string | null = useAppSelector(state => {return tokenSelector(state)})
    const[projects, setProjects] = useState<Project[]>([])

    useEffect(()=>{
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
        <List
            itemLayout="vertical"
            pagination={{pageSize: 10}}
            dataSource={projects}
            renderItem={(item: Project, index: number) => {
                return ProjectItem({item: item, index: index})
            }}
        />
    )



}

