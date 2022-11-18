import * as FlexLayout from "flexlayout-react";
import {ProjectList} from "../project/Project";
import {
    Action,
    Actions,
    DockLocation,
    IJsonModel,
    IJsonTabNode,
    IJsonTabSetNode,
    Model,
    TabNode
} from "flexlayout-react";
import {Designer} from "../designer/Designer";
import {Button, Container, Paper, Skeleton, Stack} from "@mui/material";
import {AiOutlineDeploymentUnit} from "react-icons/ai";
import {FaProjectDiagram, FaDrawPolygon} from "react-icons/fa";
import {RiFunctionLine} from "react-icons/ri";
import {VscCircuitBoard, VscExtensions} from "react-icons/vsc";
import {GrTasks} from "react-icons/gr";

function iconFatory(node: TabNode) {
    const icon = node.getIcon()
    switch (icon) {
        case "projectsIcon": {
            return (
                <FaProjectDiagram/>
            )
        }
            ;
        case "designerIcon": {
            return (
                <VscCircuitBoard/>
            )
        }
            ;
        case "metalRepoIcon": {
            return (
                <VscExtensions/>
            )
        }
            ;
        case "deploymentIcon": {
            return (
                <AiOutlineDeploymentUnit/>
            )
        }
        case "executionsIcon": {
            return (
                <GrTasks/>
            )
        }
        default: {
            return (
                <RiFunctionLine/>
            )
        }
    }
}

function factory(node: TabNode) {
    const component = node.getComponent()
    switch (component) {
        case "projects":
            return (
                <ProjectList/>
            );
        case "designer":
            return (
                <Designer/>
            );
        default:
            return (
                <div className={'panel'}>
                    <Skeleton/>
                </div>
            )
    }
}

export function Main() {
    const projectList = (
        <ProjectList/>
    )
    const designerNode: IJsonTabNode = {
        type: "tab",
        name: "Designer",
        icon: "designerIcon",
        component: "designer",
    }

    const designerNode1: IJsonTabNode = {
        type: "tab",
        name: "Designer-1",
        icon: "designerIcon",
        component: "designer",
    }

    const tabsets: IJsonTabSetNode = {
        type: "tabset",
        id: "main",
        weight: 50,
        children: [designerNode]
    }

    const layout: IJsonModel = {
        global: {"tabEnableFloat": true},
        borders: [{
            type: "border",
            location: "left",
            size: 500,
            minSize: 500,
            enableDrop: false,
            children: [{
                type: "tab",
                name: "Projects",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                icon: "projectsIcon",
                component: "projects",
            }, {
                type: "tab",
                name: "Metal Repo",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                icon: "metalRepoIcon",
                component: "empty",
            }, {
                type: "tab",
                name: "Deployment",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                icon: "deploymentIcon",
                component: "empty",
            }, {
                type: "tab",
                name: "Executions",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                icon: "executionsIcon",
                component: "empty",
            }]
        }],
        layout: {
            type: "row",
            weight: 100,
            children: [tabsets]
        }
    }
    const layoutModel: FlexLayout.Model = Model.fromJson(layout)

    const add = () => {
        const action: Action = Actions.addNode({
                type: "tab",
                name: "Designer-1",
                icon: "designerIcon",
                component: "designer",
            },
            "main",
            DockLocation.TOP,
            1
        )
        layoutModel.doAction(action)
    }

    return (
        <FlexLayout.Layout model={layoutModel} factory={factory} iconFactory={iconFatory}></FlexLayout.Layout>
    )
}