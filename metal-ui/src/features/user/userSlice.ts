import {createSlice, createAsyncThunk, PayloadAction, createSelector} from "@reduxjs/toolkit";
import {authenticate, sync, UserBasicCredentials} from "./UserApi";
import {AxiosResponse} from "axios";
import {ApiResponseEntity} from "../../api/APIs";
import {RootState} from "../../app/store";

export interface User {
    id: string,
    name: string,
    roles: string[]
}

export interface UserState {
    auth: {
        token: string | null
    }
    user: User | null
}

const initState: UserState = {
    auth: {
        token: null
    },
    user: null
}

export const tokenSelector = createSelector([(state: RootState) => {
    return state.user.auth.token
}], token => token)

export const userSlice = createSlice({
    name: 'user',
    initialState: initState,
    reducers: {
        auth: (state, action) => {
            const token: string = action.payload
            return {
                user: null,
                auth: {
                    token: token
                }
            }
        },
        user: (state, action) => {
            const user: User = action.payload
            return {
                ...state,
                user: user
            }
        }
    }
})

export const {auth, user} = userSlice.actions
export default userSlice.reducer