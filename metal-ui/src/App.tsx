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


import "./App.css";
import React from "react";
import { SignIn } from "./features/user/SignIn";
import { Main } from "./features/main/Main";
import { Paper } from "@mui/material";
import { UserBar } from "./features/user/UserBar";

function App() {
  return (
    <div className="App">
      <SignIn></SignIn>
      <div
        style={{
          display: "block",
          position: "absolute",
          height: "100%",
          width: "100%",
        }}
      >
        <Paper
          square
          variant="outlined"
          sx={{
            boxSizing: "border-box",
            height: "8vh",
            width: "100%",
            position: "absolute",
          }}
        >
          <UserBar />
        </Paper>
        <div
          style={{
            width: "100%",
            height: "92vh",
            position: "absolute",
            top: "8vh",
          }}
        >
          <Main />
        </div>
      </div>
    </div>
  );
}

export default App;
