import {createSlice} from "@reduxjs/toolkit";

export interface UserState {
    token: string | null,
    user: {
        id: string,
        name: string
    } | null
}

const initState: UserState = {
    token: null,
    user: null
}

export const userSlice = createSlice({
    name: 'user',
    initialState: initState,
    reducers: {
        login: (state, action) => {
            console.log(state)
            console.log(action)
        }
    }
})

export const {login} = userSlice.actions
export default userSlice.reducer