import {Spec} from "../../model/Spec";
import {CarryOutOutlined, CheckOutlined, FormOutlined} from '@ant-design/icons';
import {Tree} from "antd";
import type { DataNode } from 'antd/es/tree';
import {Metal} from "../../model/Metal";
const { DirectoryTree } = Tree;

function mapMetal(metal: Metal) {
    return {
        title: metal.name,
        key: metal.id,
        icon: <CarryOutOutlined/>,
        children: [
            {
                title: 'type:' + metal.type,
                icon: <CarryOutOutlined />
            },
            {
                title: 'id:' + metal.id,
                icon: <CarryOutOutlined />
            },
            {
                title: 'name:' + metal.name,
                icon: <CarryOutOutlined />
            },
            {
                title: 'type:' + metal.type,
                icon: <CarryOutOutlined />
            },
            {
                title: 'props:' + metal.props,
                icon: <CarryOutOutlined />
            }
        ]
    }
}

function mapEdge(edge: { left: string, right: string }) {
    return {
        title: edge.left + " -> " + edge.right,
        key: edge.left + " -> " + edge.right,
        icon: <CarryOutOutlined/>,
    }
}

function specToTree(spec: Spec): DataNode[] {
    const treeData: DataNode[] = [{
        title: "version:" + spec.version,
        key: "version",
        icon: <CarryOutOutlined/>
    }, {
        title: "edges",
        key: "edges",
        icon: <CarryOutOutlined/>,
        children: spec.edges.map(mapEdge)
    }, {
        title: "metals",
        key: "metals",
        icon: <CarryOutOutlined/>,
        children: spec.metals.map(mapMetal)
    }]
    return treeData
}

export interface Props {
    spec: Spec
}

export function SpecViewer(props: Props) {
    const spec = props.spec
    const treeData = specToTree(spec);
    return (
        <div>
            <Tree showIcon={true} showLine={true} treeData={treeData}>
            </Tree>
            <DirectoryTree treeData={treeData} multiple={true} ></DirectoryTree>
        </div>

    )
}