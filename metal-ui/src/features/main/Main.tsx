import * as FlexLayout from "flexlayout-react";
import { ProjectList, ProjectListProps } from "../project/Project";
import {
    Action,
    Actions,
    DockLocation,
    IJsonModel,
    IJsonTabNode,
    IJsonTabSetNode,
    Model,
    TabNode,
} from "flexlayout-react";
import { Designer, DesignerProps } from "../designer/Designer";
import { Skeleton } from "@mui/material";
import { AiOutlineDeploymentUnit } from "react-icons/ai";
import { FaProjectDiagram } from "react-icons/fa";
import { RiFunctionLine } from "react-icons/ri";
import { VscCircuitBoard, VscExtensions, VscHome, VscPreview } from "react-icons/vsc";
import { GrTasks } from "react-icons/gr";
import { ProjectStarter, ProjectStarterProps } from "../project/ProjectStarter";
import { DesignerProvider } from "../designer/DesignerProvider";
import { Home } from "../home/Home";
import { MetalRepo, MetalRepoProps } from "../repository/MetalRepo";

function iconFatory(node: TabNode) {
    const icon = node.getIcon();
    switch (icon) {
        case "homeIcon":
            return <VscHome />;
        case "projectsIcon":
            return <FaProjectDiagram />;
        case "designerIcon":
            return <VscCircuitBoard />;
        case "viewerIcon":
            return <VscPreview />;
        case "starterIcon":
            return <VscHome />;
        case "metalRepoIcon":
            return <VscExtensions />;
        case "deploymentIcon":
            return <AiOutlineDeploymentUnit />;

        case "executionsIcon":
            return <GrTasks />;

        default:
            return <RiFunctionLine />;
    }
}

export function designerId(id: string, isReadOnly: boolean | undefined) {
    return isReadOnly? `viewer[${id}]`: `designer[${id}]`;
}


export interface MainHandler {
    openProjectStarter: (props: ProjectStarterProps) => void;
    openDesigner: (props: DesignerProps) => void;
    openMetalRepo: (props: MetalRepoProps) => void;
    select: (id: string) => void;
    close?: (id: string) => void;
    renameDesigner?: (id: string, newName: string) => void;
}

export function Main() {
    const home: IJsonTabNode = {
        type: "tab",
        name: "Home",
        icon: "homeIcon",
        component: "home", 
        enableDrag: false,
        enableFloat: false,
        enableRename: false,
        enableClose: false,
    }
    
    const main: IJsonTabSetNode = {
        type: "tabset",
        id: "main",
        weight: 50,
        children: [home],
    };

    const layout: IJsonModel = {
        global: { tabEnableFloat: true },
        borders: [
            {
                type: "border",
                location: "left",
                size: 500,
                minSize: 500,
                enableDrop: false,
                children: [
                    {
                        type: "tab",
                        id: "projects_tab",
                        name: "Projects",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "projectsIcon",
                        component: "projects",
                        config: {
                            mainHandler: null
                        }
                    },
                    {
                        type: "tab",
                        id: "metal_repo_tab",
                        name: "Metal Repo",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "metalRepoIcon",
                        component: "empty",
                    },
                    {
                        type: "tab",
                        id: "deployment_tab",
                        name: "Deployment",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "deploymentIcon",
                        component: "empty",
                    },
                    {
                        type: "tab",
                        id: "executions_tab",
                        name: "Executions",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "executionsIcon",
                        component: "empty",
                    },
                ],
            },
        ],
        layout: {
            type: "row",
            weight: 100,
            id: "Main",
            children: [main],
        },
    };
    const layoutModel: FlexLayout.Model = Model.fromJson(layout);

    const openProjectStarter = (props: ProjectStarterProps) => {
        const {id} = props;
        const tab: IJsonTabNode = {
            type: "tab",
            id: id,
            name: id,
            icon: "starterIcon",
            component: "starter",
            config: props,
        }

        const action: Action = Actions.addNode(
            tab,
            "main",
            DockLocation.CENTER,
            1
        );
        layoutModel.doAction(action);
    }

    const openDesigner = (props: DesignerProps) => {
        const { id, name, isReadOnly } = props;
        const tab: IJsonTabNode = {
            type: "tab",
            id: designerId(id, isReadOnly),
            name: name === undefined? `Project[${id}]`: `Project[${name}]`,
            icon: isReadOnly? "viewerIcon": "designerIcon",
            component: "designer",
            config: props,
        }

        const action: Action = Actions.addNode(
            tab,
            "main",
            DockLocation.CENTER,
            1
        );
        layoutModel.doAction(action);
    };

    const close = (id: string) => {
        const action: Action = Actions.deleteTab(id);
        layoutModel.doAction(action);
    }

    const renameDesigner = (id: string, newName: string) => {
        const action: Action = Actions.renameTab(id, newName);
        layoutModel.doAction(action);
    }

    const select = (id: string) => {
        const action: Action = Actions.selectTab(id);
        layoutModel.doAction(action);
    }

    const openMetalRepo = (props: MetalRepoProps) => {
        const tab: IJsonTabNode = {
            type: "tab",
            name: "Repository",
            icon: "metalRepoIcon",
            component: "metalRepo",
            config: props,
        }
        const action: Action = Actions.addNode(
            tab,
            "main",
            DockLocation.CENTER,
            1
        );
        layoutModel.doAction(action);
    }

    const mainHandler: MainHandler = {
        openProjectStarter: openProjectStarter,
        openDesigner: openDesigner,
        openMetalRepo: openMetalRepo,
        select: select,
        close: close,
        renameDesigner: renameDesigner,
    }

    const factory = (node: TabNode) => {
        const component = node.getComponent();
        const config = node.getConfig();
        switch (component) {
            case "projects": {
                const props: ProjectListProps = {
                    ...config,
                    mainHandler: mainHandler
                };
                return <ProjectList {...props}/>;
            }

            case "starter": {
                const props: ProjectStarterProps = {
                    ...config,
                    mainHandler: mainHandler
                };
                return <ProjectStarter {...props}/>;
            }
                
            case "designer": {
                const props: DesignerProps = config;
                return (
                    <DesignerProvider>
                        <Designer {...props} mainHandler={mainHandler}/>
                    </DesignerProvider>
                );
            }

            case "metalRepo": {
                const props: MetalRepoProps = config;
                return (
                    <MetalRepo {...props} />
                )
            }

            case "home": {
                return (
                    <Home mainHandler={mainHandler}/>
                )
            }

            default:
                return (
                    <div className={"panel"}>
                        <Skeleton />
                    </div>
                );
        }
    };

    return (
        <FlexLayout.Layout
            model={layoutModel}
            factory={factory}
            iconFactory={iconFatory}
        ></FlexLayout.Layout>
    );
}
