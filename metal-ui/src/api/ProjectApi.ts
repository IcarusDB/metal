import axios, { AxiosError } from "axios";
import {ApiResponse, ApiResponseEntity, timeout} from "./APIs";
import {BackendStatus, Deploy, Project} from "../model/Project";
import _ from "lodash"
import { Spec } from "../model/Spec";
import { idMap } from "./IdMap";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

function projectMap (obj: any): Project {
    obj.user = _.mapKeys(obj.user, (val, key) => {
        return key === '_id'? 'id': key
    })

    obj = _.mapKeys(obj, (val, key) => {
        return key === '_id'? 'id': key
    })
    const proj: Project = obj
    return proj
}

export async function getAllProjectOfUser(token: string): Promise<Project[]> {
    const url = '/api/v1/projects'
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (!ApiResponse.isSuccess(resp)) {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
            if (resp.data === undefined) {
                throw new Error('Response is successful, but no data found in response.')
            }
            const result: Project[] = resp.data.map(projectMap)
            const projects: Project[] = result
            return projects
        } catch (err) {
            return Promise.reject(err)
        }
    })
}

export async function getProjectById(token: string, id:string) {
    const url = `/api/v1/projects/id/${id}`;
    return instance.get(url, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (!ApiResponse.isSuccess(resp)) {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
            if (resp.data === undefined) {
                throw new Error('Response is successful, but no data found in response.')
            }
            const project: Project = projectMap(resp.data);
            return project;
        } catch (err) {
            return Promise.reject(err)
        }
    })
}



export interface ProjectParams {
    name?: string,
    pkgs?: string[],
    platform?: any;
    backendArgs?: string[],
}

export async function updateProjectDetail(token:string, id: string, params: ProjectParams) {
    const url = `/api/v1/projects/id/${id}`;
    return instance.put(url, params, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (!ApiResponse.isSuccess(resp)) {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
            if (resp.data === undefined) {
                throw new Error('Response is successful, but no data found in response.')
            }
            return id;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export async function createProject(token:string, params: ProjectParams) {
    const url = "/api/v1/projects";
    return instance.post(url, params, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (!ApiResponse.isSuccess(resp)) {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
            if (resp.data === undefined) {
                throw new Error('Response is successful, but no data found in response.')
            }
            const projectId: string = resp.data;
            return projectId;
        } catch (err) {
            return Promise.reject(err)
        }
    });
}

export async function updateProject(token:string, id: string, params: ProjectParams) {
    const url = `/api/v1/projects/id/${id}`;
    return instance.put(url, params, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (!ApiResponse.isSuccess(resp)) {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
            if (resp.data === undefined) {
                throw new Error('Response is successful, but no data found in response.')
            }
            const projectId: string = resp.data;
            return projectId;
        } catch (err) {
            return Promise.reject(err)
        }
    });
}

export async function getDeploy(token:string, deployId: string) {
    const url = `/api/v1/projects/deploy/${deployId}`;
    return instance.get(url, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp);
            const deploy: Deploy = idMap<Deploy>(resp.data);
            return deploy;
        } catch (err) {
            return Promise.reject(err)
        }
    });
}

export async function getBackendStatus(token: string, deployId: string) {
    const url = `/api/v1/projects/deploy/${deployId}/backend/status`;
    return instance.get(url, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const status: BackendStatus= resp.data;
            return status;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export interface DeployResponse {
    success: boolean,
    submissionId?: string,
    message?: string,
}

export async function deployBackendOfId(token: string, deployId: string) {
    const url = `/api/v1/projects/deploy/${deployId}`;
    return instance.post(url, undefined, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const deployResp: DeployResponse = resp.data;
            return deployResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export async function redeployBackendOfId(token: string, deployId: string) {
    const url = `/api/v1/projects/deploy/${deployId}/epoch`;
    return instance.put(url, undefined, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const deployResp: DeployResponse = resp.data;
            return deployResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}


export interface UnDeployResponse extends DeployResponse {
}

export async function undeployBackendOfId(token: string, deployId: string) {
    const url = `/api/v1/projects/deploy/${deployId}/force`;
    return instance.delete(url, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const undeployResp: UnDeployResponse = resp.data;
            return undeployResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export interface AnalysisResponse {
    analysed: string[],
    unAnalysed: string[],
}

export async function analysisOfId(token: string, id: string, spec: Spec) {
    const url = `/api/v1/projects/id/${id}/spec`;
    return instance.post(url, {
        spec: spec
    }, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const analysisResp: AnalysisResponse = resp.data;
            return analysisResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export interface ExecResponse {
    status: string | "OK",
}

export async function execOfId(token: string, id: string) {
    const url = `/api/v1/projects/id/${id}/spec/current/exec`;
    return instance.post(url, undefined, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const execResp: ExecResponse = resp.data;
            return execResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}