import axios from "axios";
import _ from "lodash";
import { ApiResponse, ApiResponseEntity, timeout } from "../../../api/APIs";
import { MetalPkg } from "../../../model/MetalPkg";

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