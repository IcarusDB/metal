import axios, {AxiosBasicCredentials} from "axios";
import {ApiResponse, ApiResponseEntity, timeout} from "./APIs";
import {User} from "../model/User";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

export interface UserBasicCredentials extends AxiosBasicCredentials {}

export async function authenticate(user: UserBasicCredentials): Promise<string> {
    const url = '/api/v1/tokens'
    return instance.post(url, {}, {
        auth: user
    }).then(response => {
        try {
            const token: string = response.data.jwt;
            return token
        } catch (err) {
            return Promise.reject(err)
        }
    })
}

export async function sync(token: string): Promise<User> {
    const url = '/api/v1/user'
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            if (ApiResponse.isSuccess(resp)) {
                const user: User = resp.data
                return user
            } else {
                if (resp.msg === undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                throw new Error(resp.msg)
            }
        } catch (err) {
            return Promise.reject(err)
        }
    })
}

export interface UpdateNameRequest {
    newName: string
}
export interface UpdateNameResponse {

}

export async function updateName(token: string, newName: string): Promise<UpdateNameResponse> {
    const url = "/api/v1/user/name";
    const request: UpdateNameRequest = {
        newName: newName,
    }
    return instance.put(url, request, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    ).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const updateResp: UpdateNameResponse = resp.data;
            return updateResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}

export interface UpdatePasswordRequest {
    oldPassword: string,
    newPassword: string
}

export interface UpdatePasswordResponse {

}

export async function updatePassword(token: string, oldPassword: string, newPassword: string): Promise<UpdatePasswordResponse> {
    const url = "/api/v1/user/password";
    const request: UpdatePasswordRequest = {
        oldPassword: oldPassword,
        newPassword: newPassword,
    };
    return instance.put(url, request, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    ).then(response => {
        try {
            const resp: ApiResponseEntity = response.data;
            ApiResponse.mayBeFailure(resp);
            const updateResp: UpdatePasswordResponse = resp.data;
            return updateResp;
        } catch (err) {
            return Promise.reject(err);
        }
    })
}