import './App.css';
import React from 'react';
import {SignIn} from "./features/user/SignIn";
import {Main} from "./features/main/Main";
import { Paper } from '@mui/material';
import { UserBar } from './features/user/UserBar';

function App() {
    return (
        <div className="App">
            <SignIn></SignIn>
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
                    <Main/>
                </div>
            </div>
        </div>
    );
}

export default App;
