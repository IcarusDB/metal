import axios from "axios";
import {baseUrl, timeout, wrapUrl} from "../../api/APIs";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})

export async function token(user: {username: string, password: string}) {
    const url = '/api/v1/tokens'
    return instance.post(url, {}, {
        auth: user
    })
}