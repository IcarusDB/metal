import './App.css';
import React from 'react';
import {Login} from "./features/user/Login";
import {UserNotice} from "./features/user/UserNotice";
import {Main} from "./features/main/Main";

function App() {
    return (
        <div className="App">
            {/*<Login></Login>*/}
            {/*<UserNotice/>*/}
            <Main/>
        </div>
    );
}

export default App;
