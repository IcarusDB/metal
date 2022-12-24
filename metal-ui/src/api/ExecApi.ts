import axios from "axios";
import { Exec } from "../model/Exec";
import { ApiResponse, ApiResponseEntity, timeout } from "./APIs";
import { idMap } from "./IdMap";

const instance = axios.create({
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: timeout()
})


export async function getAllExecsOfUser(token: string): Promise<Exec[]> {
    const url = `/api/v1/execs/detail`;
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp);
            const execs: Exec[] = resp.data.map(idMap<Exec>);
            return execs;
        } catch (err) {
            return Promise.reject(err);
        }
    });
}

export async function getExecOfId(token: string, id: string): Promise<Exec> {
    const url = `/api/v1/execs/id/${id}/detail`;
    return instance.get(url, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    }).then(response => {
        try {
            const resp: ApiResponseEntity = response.data
            ApiResponse.mayBeFailure(resp);
            const exec: Exec = idMap<Exec>(resp.data);
            return exec;
        } catch (err) {
            return Promise.reject(err);
        }
    });
}
