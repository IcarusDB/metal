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

import "./SignIn.css";
import React, { useEffect, useState } from "react";
import { authenticate, UserBasicCredentials } from "../../api/UserApi";
import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { auth, tokenSelector } from "./userSlice";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import {
    Alert,
    Avatar,
    Box,
    Button,
    Typography,
    TextField,
    FormControlLabel,
    Checkbox,
    Modal,
    CircularProgress,
    Paper,
} from "@mui/material";
import { TokenUser } from "./UserBar";
import jwtDecode from "jwt-decode";
import { useUserAction } from "./UserStore";

enum State {
    idle,
    pending,
    success,
    fail,
}

const theme = createTheme();

function SignInForm() {
    const dispatch = useAppDispatch();
    const [state, setState] = useState(State.idle);
    const [token, setToken] = useState<string | null>(null);
    const {setUser} = useUserAction();
    const isPending = () => {
        return state === State.pending;
    };
    const isFail = () => {
        return state === State.fail;
    };

    const onFinish = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        const usernameEntry = data.get("username") || "";
        const passwordEntry = data.get("password") || "";
        const username = usernameEntry instanceof File ? "" : usernameEntry;
        const password = passwordEntry instanceof File ? "" : passwordEntry;
        const user: UserBasicCredentials = {
            username: username,
            password: password,
        };

        setState(State.pending);
        authenticate(user).then(
            (_token: string) => {
                setState(State.success);
                setToken(_token);
                const user: TokenUser = _token === null? {
                    username: "Off Line",
                    _id: "*",
                    iat: -1,
                }: jwtDecode(_token);
                setUser({
                    id: user._id,
                    name: user.username,
                    roles: []
                });
            },
            (reason) => {
                setState(State.fail);
                console.error(reason);
            }
        );
    };

    useEffect(() => {
        dispatch(auth(token));
    }, [dispatch, token]);

    return (
        <Box
            sx={{
                marginTop: 8,
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                minWidth: "30vw",
            }}
        >
            {isFail() && <Alert severity={"error"}>{"Fail to Sign in."}</Alert>}
            <Avatar sx={{ m: 1, bgcolor: "secondary.main" }} src="/images/metal.png">
            </Avatar>
            <Typography component="h1" variant="h5">
                Sign in
            </Typography>
            <Box component={"form"} onSubmit={onFinish} noValidate sx={{ mt: 1 }}>
                <TextField
                    margin={"normal"}
                    required
                    fullWidth
                    id={"username"}
                    name={"username"}
                    label={"User Name"}
                    disabled={isPending()}
                    autoFocus
                />
                <TextField
                    margin={"normal"}
                    required
                    fullWidth
                    id={"password"}
                    label={"Password"}
                    name={"password"}
                    type={"password"}
                    disabled={isPending()}
                />
                <FormControlLabel
                    control={
                        <Checkbox
                            value="remember"
                            color="primary"
                            disabled={state === State.pending}
                        />
                    }
                    label="Remember me"
                />
                <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    disabled={isPending()}
                    sx={{ mt: 3, mb: 2, alignItems: "center" }}
                >
                    Sign In
                    {isPending() && (
                        <CircularProgress
                            sx={{
                                position: "absolute",
                            }}
                        />
                    )}
                </Button>
            </Box>
        </Box>
    );
}

export function SignIn() {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    const isOn = token == null;

    if (!isOn) {
        return <></>;
    }

    return (
        <ThemeProvider theme={theme}>
            <Modal open={isOn} onClose={() => {}}>
                <div
                    style={{
                        height: "100%",
                        width: "100%",
                        backgroundColor: "white",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <div
                        style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "center",
                            height: "100%",
                        }}
                    >
                        <Paper
                            elevation={10}
                            sx={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                justifyContent: "center",
                                // width: "100%",
                                maxWidth: "60vw",
                                minWidth: "50vw",
                                boxSizing: "border-box",
                                paddingLeft: "1vw",
                                paddingRight: "1vw",
                                paddingTop: "1vh",
                                paddingBottom: "1vh",
                                minHeight: "50vh",
                            }}
                        >
                            <img
                                src="/images/metal_logo.png"
                                alt="logo"
                                style={{
                                    boxSizing: "border-box",
                                    paddingRight: "3vw",
                                    paddingLeft: "1vw",
                                }}
                            />
                            <SignInForm />
                        </Paper>
                    </div>
                </div>
            </Modal>
        </ThemeProvider>
    );
}
