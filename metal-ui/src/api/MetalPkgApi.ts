import axios from "axios";
import _ from "lodash";
import { ApiResponse, ApiResponseEntity, timeout } from "./APIs";
import { MetalManifest, MetalPkg, Scope } from "../model/MetalPkg";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

export async function getAllMetalPkgsOfUserAccess(token: string): Promise<MetalPkg[]> {
    const url = '/api/v1/metalRepo/all'
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp)

            const result: MetalPkg[] = resp.data.map((obj: any) => {
                obj =  _.mapKeys(obj, (val, key) => {
                    return key === '_id'? 'id': key
                })
                const pkg: MetalPkg = obj
                return pkg
            })
            return result
        } catch (err) {
            return Promise.reject(err)
        }
    })
}

export async function getAllMetalPkgsOfClasses(token: string, classes: string[]): Promise<MetalPkg[]> {
    const url = '/api/v1/metalRepo/classes'
    return instance.post(url, {
        "classes": classes
    }, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp)

            const result: MetalPkg[] = resp.data.map((obj: any) => {
                obj =  _.mapKeys(obj, (val, key) => {
                    return key === '_id'? 'id': key
                })
                const pkg: MetalPkg = obj
                return pkg
            })
            return result
        } catch (err) {
            return Promise.reject(err)
        }
    })
    
}

export async function addMetalPkgsFromManifest(token: string, scope: Scope, manifest: MetalManifest): Promise<void> {    
    const url = `api/v1/metalRepo/scope/${scope}`
    return instance.post(url, {
        "manifest": manifest
    }, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp)
        } catch (err) {
            return Promise.reject(err)
        }
    })
}