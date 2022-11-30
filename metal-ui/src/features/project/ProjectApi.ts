import axios from "axios";
import {ApiResponse, ApiResponseEntity, timeout} from "../../api/APIs";
import {Project} from "../../model/Project";
import _ from "lodash"
import { ProjectProfileValue } from "./ProjectProfile";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

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
            const result: Project[] = resp.data.map((obj: any) => {
                obj.user = _.mapKeys(obj.user, (val, key) => {
                    return key === '_id'? 'id': key
                })

                obj = _.mapKeys(obj, (val, key) => {
                    return key === '_id'? 'id': key
                })
                const proj: Project = obj
                return proj
            })

            const projects: Project[] = result
            return projects
        } catch (err) {
            return Promise.reject(err)
        }
    })
}

export async function createProject(token:string, profile: ProjectProfileValue) {
    const url = "/api/v1/projects";
    return instance.post(url, profile, {
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

export async function updateProject(token:string, profile: ProjectProfileValue) {
    const url = "/api/v1/projects";
    return instance.post(url, profile, {
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