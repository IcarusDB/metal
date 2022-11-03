import React from 'react';
import logo from './logo.svg';
import {Counter} from './features/counter/Counter';
import './App.css';
import {Designer} from "./features/designer/Designer";
import {SpecViewer} from "./features/spec/SpecViewer";
import {Spec} from "./model/Spec";
import {Login} from "./features/user/Login";
import {UserNotice} from "./features/user/UserNotice";

function App() {
    const spec: Spec = {
        "version": "1.0",
        "metals": [
            {
                "type": "org.metal.backend.spark.extension.JsonFileMSource",
                "id": "00-00",
                "name": "source-00",
                "props": {
                    "schema": "",
                    "path": "src/test/resources/test.json"
                }
            },
            {
                "type": "org.metal.backend.spark.extension.SqlMMapper",
                "id": "01-00",
                "name": "mapper-00",
                "props": {
                    "tableAlias": "source",
                    "sql": "select * from source where id != \"0001\""
                }
            },
            {
                "type": "org.metal.backend.spark.extension.ConsoleMSink",
                "id": "02-00",
                "name": "sink-00",
                "props": {
                    "numRows": 10
                }
            },
            {
                "type": "org.metal.backend.spark.extension.ConsoleMSink",
                "id": "02-01",
                "name": "sink-01",
                "props": {
                    "numRows": 10
                }
            }
        ],
        "edges": [
            {
                "left": "00-00",
                "right": "01-00"
            },
            {
                "left": "01-00",
                "right": "02-00"
            },
            {
                "left": "00-00",
                "right": "02-01"
            }
        ],
        "waitFor": [
            {
                "left": "02-00",
                "right": "02-01"
            }
        ]
    }
    return (
        <div className="App">
            <Login></Login>
            <UserNotice/>
        </div>
    );
}

export default App;
