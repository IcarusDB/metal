import { MainHandler } from "../main/Main";
import { ProjectProfile } from "./ProjectProfile";

export interface ProjectStarterProps {
    id: string,
    mainHandler: MainHandler
}

export function ProjectStarter(props: ProjectStarterProps) {
    const {id, mainHandler} = props;

    const onCreateFinish = (projectId: string) => {
        if (mainHandler.close !== undefined) {
            mainHandler.close(id);
            
        }
    }
    return (
        <ProjectProfile open={true} isCreate={true} onFinish={onCreateFinish}/>
    )
}