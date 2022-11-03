import axios, {AxiosBasicCredentials} from "axios";
import {ApiResponse, ApiResponseEntity, ApiResponseStatus, timeout} from "../../api/APIs";
import {User} from "../../model/User";

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
                if (resp.msg == undefined) {
                    throw new Error('Response is failure, and no msg found in response.')
                }
                const msg: string = resp.msg
                throw new Error(resp.msg)
            }
        } catch (err) {
            return Promise.reject(err)
        }
    })
}