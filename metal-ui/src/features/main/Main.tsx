/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
import {
  VscAccount,
  VscBrowser,
  VscCircuitBoard,
  VscExtensions,
  VscHome,
  VscPackage,
  VscPreview,
} from "react-icons/vsc";
import { GrTasks } from "react-icons/gr";
import { ProjectStarter, ProjectStarterProps } from "../project/ProjectStarter";
import { DesignerProvider } from "../designer/DesignerProvider";
import { Home, HomeProps } from "../home/Home";
import {
  MetalPkgPage,
  MetalPkgPageProps,
  MetalRepo,
  MetalRepoProps,
} from "../repository/MetalRepo";
import create from "zustand";
import { subscribeWithSelector } from "zustand/middleware";
import _ from "lodash";
import { Executions, ExecutionsProps } from "../execution/Executions";
import { Viewer, ViewerProps } from "../designer/Viewer";
import { ExecutionPage, ExecutionPageProps } from "../execution/ExecutionPage";
import { UserPage, UserPageProps } from "../user/UserPage";
import { MetalPkgProps } from "../designer/explorer/MetalExplorer";

interface Component {
  id: string;
  type: string;
  props: any;
  instance: JSX.Element;
}

interface ComponentFactory {
  components: Component[];
  memorize: (
    type: string,
    props: any,
    cmpFactory: () => JSX.Element,
    id: string
  ) => JSX.Element;
  destory: (equal: (cmp: Component) => boolean) => void;
}

const useComponentFactory = create<ComponentFactory>()(
  subscribeWithSelector((set, get) => ({
    components: [],
    memorize: (type, props, cmpFactory, id) => {
      const mCmps = get().components.filter((component) => {
        if (component.type !== type) {
          return false;
        }

        return _.isEqualWith(props, component.props);
      });
      if (mCmps.length === 0) {
        const newCmp = cmpFactory();
        set((prev) => ({
          components: [
            { id: id, type: type, props: props, instance: newCmp },
            ...prev.components,
          ],
        }));
        return newCmp;
      } else {
        return mCmps[0].instance;
      }
    },
    destory: (equal: (cmp: Component) => boolean) => {
      set((prev) => ({
        components: _.dropWhile(prev.components, equal),
      }));
    },
  }))
);

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
    case "metalPkgPageIcon":
      return <VscPackage />;
    case "deploymentIcon":
      return <AiOutlineDeploymentUnit />;
    case "executionPageIcon":
      return <VscBrowser />;
    case "executionsIcon":
      return <GrTasks />;
    case "userPageIcon":
      return <VscAccount />;

    default:
      return <RiFunctionLine />;
  }
}

export function designerId(id: string) {
  return `designer[${id}]`;
}

export function viewerId(id: string) {
  return `viewer[${id}]`;
}

export function execPageId(id: string) {
  return `exec[${id}]`;
}

export function metalPkgPageId(id: string) {
  return `metal pkg[${id}]`;
}

export interface MainHandler {
  openProjectStarter: (props: ProjectStarterProps) => void;
  openDesigner: (props: DesignerProps) => void;
  openViewer: (props: ViewerProps) => void;
  openMetalRepo: (props: MetalRepoProps) => void;
  openMetalPkgPage: (props: MetalPkgPageProps) => void;
  openExecutionPage: (props: ExecutionPageProps) => void;
  openUserPage: (props: UserPageProps) => void;
  select: (id: string) => void;
  close?: (id: string) => void;
  rename?: (id: string, newName: string) => void;
}

export function Main() {
  const memorizeCmps = useComponentFactory((state) => state.memorize);
  const destoryCmp = useComponentFactory((state) => state.destory);
  const home: IJsonTabNode = {
    type: "tab",
    name: "Home",
    icon: "homeIcon",
    component: "home",
    enableDrag: false,
    enableFloat: false,
    enableRename: false,
    enableClose: false,
  };

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
              mainHandler: null,
            },
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
            component: "executions",
            config: {
              mainHandler: null,
            },
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
    const { id } = props;
    const tab: IJsonTabNode = {
      type: "tab",
      id: id,
      name: id,
      icon: "starterIcon",
      component: "starter",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const openDesigner = (props: DesignerProps) => {
    const { id } = props;
    const tab: IJsonTabNode = {
      type: "tab",
      id: designerId(id),
      name: `Designer[${id}]`,
      icon: "designerIcon",
      component: "designer",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const openViewer = (props: ViewerProps) => {
    const { id } = props;
    const tab: IJsonTabNode = {
      type: "tab",
      id: viewerId(id),
      name: `Viewer[${id}]`,
      icon: "viewerIcon",
      component: "viewer",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const openExecutionPage = (props: ExecutionPageProps) => {
    const { id } = props;
    const tab: IJsonTabNode = {
      type: "tab",
      id: execPageId(id),
      name: `Exec[${id}]`,
      icon: "executionPageIcon",
      component: "executionPage",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const openUserPage = (props: UserPageProps) => {
    const tab: IJsonTabNode = {
      type: "tab",
      id: "User",
      name: "User",
      icon: "userPageIcon",
      component: "userPage",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const openMetalPkgPage = (props: MetalPkgPageProps) => {
    const { id } = props.pkg;
    const tab: IJsonTabNode = {
      type: "tab",
      id: metalPkgPageId(id),
      name: `Metal[${props.pkg.class}]`,
      icon: "metalPkgPageIcon",
      component: "metalPkgPage",
      config: props,
    };

    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const close = (id: string) => {
    const action: Action = Actions.deleteTab(id);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
    }
  };

  const rename = (id: string, newName: string) => {
    const action: Action = Actions.renameTab(id, newName);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
    }
  };

  const select = (id: string) => {
    const action: Action = Actions.selectTab(id);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
    }
  };

  const openMetalRepo = (props: MetalRepoProps) => {
    const tab: IJsonTabNode = {
      id: "metal-repository",
      type: "tab",
      name: "Repository",
      icon: "metalRepoIcon",
      component: "metalRepo",
      config: props,
    };
    const action: Action = Actions.addNode(tab, "main", DockLocation.CENTER, 1);
    try {
      layoutModel.doAction(action);
    } catch (error) {
      console.error(error);
      if (
        (error as Error).message.startsWith(
          "Error: each node must have a unique id"
        ) &&
        tab.id !== undefined
      ) {
        select(tab.id);
      }
    }
  };

  const mainHandler: MainHandler = {
    openProjectStarter: openProjectStarter,
    openDesigner: openDesigner,
    openViewer: openViewer,
    openMetalRepo: openMetalRepo,
    openMetalPkgPage: openMetalPkgPage,
    openExecutionPage: openExecutionPage,
    openUserPage: openUserPage,
    select: select,
    close: close,
    rename: rename,
  };

  const factory = (node: TabNode) => {
    const id = node.getId();
    const component = node.getComponent();
    const config = node.getConfig();
    switch (component) {
      case "projects": {
        const props: ProjectListProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <ProjectList {...props} />,
          id
        );
      }

      case "starter": {
        const props: ProjectStarterProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <ProjectStarter {...props} />,
          id
        );
      }

      case "designer": {
        const props: DesignerProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => (
            <DesignerProvider>
              <Designer {...props} />
            </DesignerProvider>
          ),
          id
        );
      }

      case "viewer": {
        const props: ViewerProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => (
            <DesignerProvider>
              <Viewer {...props} />
            </DesignerProvider>
          ),
          id
        );
      }

      case "metalRepo": {
        const props: MetalRepoProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <MetalRepo {...props} />,
          id
        );
      }

      case "metalPkgPage": {
        const props: MetalPkgPageProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <MetalPkgPage {...props} />,
          id
        );
      }

      case "executionPage": {
        const props: ExecutionPageProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <ExecutionPage {...props} />,
          id
        );
      }

      case "executions": {
        const props: ExecutionsProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <Executions {...props} />,
          id
        );
      }

      case "userPage": {
        const props: UserPageProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(
          component,
          props,
          () => <UserPage {...props} />,
          id
        );
      }

      case "home": {
        const props: HomeProps = {
          ...config,
          mainHandler: mainHandler,
        };
        return memorizeCmps(component, props, () => <Home {...props} />, id);
      }

      default:
        return (
          <div className={"panel"}>
            <Skeleton />
          </div>
        );
    }
  };

  const onRecycle = (action: FlexLayout.Action) => {
    if (action.type === FlexLayout.Actions.DELETE_TAB) {
      const id: string = action.data["node"];
      destoryCmp((cmp: Component) => cmp.id === id);
    }
    return action;
  };

  return (
    <FlexLayout.Layout
      model={layoutModel}
      factory={factory}
      iconFactory={iconFatory}
      onAction={onRecycle}
    ></FlexLayout.Layout>
  );
}
