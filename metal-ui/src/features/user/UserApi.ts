import axios, {AxiosBasicCredentials} from "axios";
import {timeout} from "../../api/APIs";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

export interface UserBasicCredentials extends AxiosBasicCredentials {}

export async function authenticate(user: UserBasicCredentials) {
    const url = '/api/v1/tokens'
    return instance.post(url, {}, {
        auth: user
    })
}

export async function sync(token: string) {
    const url = '/api/v1/user'
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
}