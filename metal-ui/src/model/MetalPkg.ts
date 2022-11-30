export interface MetalPkg {
    id: string,
    userId: string,
    type: string,
    scope: "PRIVATE" | "PUBLIC",
    createTime: number,
    pkg: string,
    class: string,
    groupId: string,
    artifactId: string,
    version: string,
    formSchema: any,
    uiSchema?: any,
    description?: any
}