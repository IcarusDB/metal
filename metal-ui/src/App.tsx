import './App.css';
import React from 'react';
import {SignIn} from "./features/user/SignIn";
import {UserNotice} from "./features/user/UserNotice";
import {Main} from "./features/main/Main";

function App() {
    return (
        <div className="App">
            <SignIn></SignIn>
            {/*<UserNotice/>*/}
            <Main/>
        </div>
    );
}

export default App;
