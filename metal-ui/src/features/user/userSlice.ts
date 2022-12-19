import {createSelector, createSlice} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {User} from "../../model/User";

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
        },
        logout: (state, action) => {
            return initState;
        }
    }
})

export const {auth, user, logout} = userSlice.actions
export default userSlice.reducer