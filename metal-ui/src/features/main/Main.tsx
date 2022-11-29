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
import { Button, Container, Paper, Skeleton, Stack } from "@mui/material";
import { AiOutlineDeploymentUnit } from "react-icons/ai";
import { FaProjectDiagram, FaDrawPolygon } from "react-icons/fa";
import { RiFunctionLine } from "react-icons/ri";
import { VscCircuitBoard, VscExtensions } from "react-icons/vsc";
import { GrTasks } from "react-icons/gr";
import { useMemo } from "react";

function iconFatory(node: TabNode) {
    const icon = node.getIcon();
    switch (icon) {
        case "projectsIcon":
            return <FaProjectDiagram />;
        case "designerIcon":
            return <VscCircuitBoard />;
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

export interface MainHandler {
    openDesigner: (props: DesignerProps) => void;
}

export function Main() {
    // const home = {
    //     type: "tab",
    //     name: "Home",
    //     icon: "executionsIcon",
    //     component: "home",
    
    // }
    const main: IJsonTabSetNode = {
        type: "tabset",
        id: "main",
        weight: 50,
        children: [],
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
                        name: "Metal Repo",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "metalRepoIcon",
                        component: "empty",
                    },
                    {
                        type: "tab",
                        name: "Deployment",
                        enableDrag: false,
                        enableClose: false,
                        enableFloat: false,
                        icon: "deploymentIcon",
                        component: "empty",
                    },
                    {
                        type: "tab",
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

    const openDesigner = (props: DesignerProps) => {
        const { project } = props;

        const action: Action = Actions.addNode(
            {
                type: "tab",
                name: project === undefined? "new": project.name,
                icon: "designerIcon",
                component: "designer",
                config: props,
            },
            "Main",
            DockLocation.TOP,
            1
        );
        layoutModel.doAction(action);
    };

    const mainHandler: MainHandler = {
        openDesigner: openDesigner,
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
                
            case "designer": {
                const props: DesignerProps = config;
                return (
                    <Designer {...props} />
                );
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
