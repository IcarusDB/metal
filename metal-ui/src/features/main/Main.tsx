import * as FlexLayout from "flexlayout-react";
import {ProjectList} from "../project/Project";
import {Empty} from "antd";
import {IJsonModel, Model, TabNode} from "flexlayout-react";
import {Designer} from "../designer/Designer";

function factory(node: TabNode) {
    const component = node.getComponent()
    switch (component) {
        case "projects": {
            return (
                <ProjectList/>
            )
        };
        case "designer": {
            return (
                <Designer/>
            )
        }
        default: {
            return (
                <div className={'panel'}>
                    <Empty/>
                </div>
            )
        }
    }
}

export function Main() {
    const projectList = (
        <ProjectList/>
    )
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
                component: "projects",
            }, {
                type: "tab",
                name: "Metal Repo",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                component: "empty",
            }, {
                type: "tab",
                name: "Deployment",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                component: "empty",
            }, {
                type: "tab",
                name: "Executions",
                enableDrag: false,
                enableClose: false,
                enableFloat: false,
                component: "empty",
            }]
        }],
        layout: {
            type: "row",
            weight: 100,
            children: [{
                type: "tabset",
                weight: 50,
                children: [{
                    type: "tab",
                    name: "Designer",
                    component: "designer",
                }]
            }]
        }
    }
    return (
        <FlexLayout.Layout model={Model.fromJson(layout)} factory={factory}></FlexLayout.Layout>
    )
}