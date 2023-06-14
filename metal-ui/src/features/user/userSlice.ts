/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import { createSelector, createSlice } from "@reduxjs/toolkit";
import { RootState } from "../../app/store";
import { User } from "../../model/User";

export interface UserState {
  auth: {
    token: string | null;
  };
  user: User | null;
}

const initState: UserState = {
  auth: {
    token: null,
  },
  user: null,
};

export const tokenSelector = createSelector(
  [
    (state: RootState) => {
      return state.user.auth.token;
    },
  ],
  (token) => token
);

export const userSlice = createSlice({
  name: "user",
  initialState: initState,
  reducers: {
    auth: (state, action) => {
      const token: string = action.payload;
      return {
        user: null,
        auth: {
          token: token,
        },
      };
    },
    user: (state, action) => {
      const user: User = action.payload;
      return {
        ...state,
        user: user,
      };
    },
    logout: (state, action) => {
      return initState;
    },
  },
});

export const { auth, user, logout } = userSlice.actions;
export default userSlice.reducer;
