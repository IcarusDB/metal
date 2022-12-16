import './App.css';
import React from 'react';
import {SignIn} from "./features/user/SignIn";
import {Main} from "./features/main/Main";
import { Paper } from '@mui/material';
import { height, width } from '@mui/system';

function App() {
    return (
        <div className="App">
            <SignIn></SignIn>
            {/*<UserNotice/>*/}
            <div
                style={{
                    display: 'block',
                    position: "absolute",
                    height: "100%",
                    width: "100%",
                }}
            >
                <Paper
                    square
                    variant='outlined'
                    sx={{
                        boxSizing: "border-box",
                        height: "5vh",
                        width: "100%",
                        position: "absolute",
                    }}
                >

                </Paper>
                <div
                    style={{
                        width: "100%",
                        height: "95vh",
                        position: "absolute",
                        top: "5vh",
                    }}
                >
                    <Main/>
                </div>
            </div>
        </div>
    );
}

export default App;
