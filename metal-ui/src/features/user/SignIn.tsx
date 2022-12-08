import './SignIn.css'
import React, {useEffect, useRef, useState} from "react";
import {authenticate, UserBasicCredentials} from "../../api/UserApi";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {auth, tokenSelector} from "./userSlice";
import {createTheme, ThemeProvider} from '@mui/material/styles';
import {
    Alert,
    Avatar,
    Box,
    Button,
    Container,
    CssBaseline,
    Typography,
    TextField,
    FormControlLabel,
    Checkbox,
    Modal,
    CircularProgress,
    Backdrop
} from "@mui/material";
import {LockOutlined} from "@mui/icons-material";

enum State {
    idle,
    pending,
    success,
    fail
}

const theme = createTheme();

function SignInForm() {
    const dispatch = useAppDispatch()
    const [state, setState] = useState(State.idle)
    const [token, setToken] = useState<string | null>(null)
    const isPending = () => {
        return state === State.pending
    }
    const isFail = () => {
        return state === State.fail
    }

    const onFinish = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        const usernameEntry = data.get('username') || ""
        const passwordEntry = data.get('password') || ""
        const username = (usernameEntry instanceof File) ? "" : usernameEntry;
        const password = (passwordEntry instanceof File) ? "" : passwordEntry;
        const user: UserBasicCredentials = {
            username: username,
            password: password
        }

        setState(State.pending)
        authenticate(user).then((_token: string) => {
            setState(State.success)
            setToken(_token)
        }, reason => {
            setState(State.fail)
            console.error(reason)
        })
    }

    useEffect(() => {
        dispatch(auth(token))
    }, [token])

    return (
        <Box
            sx={{
                marginTop: 8,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
            }}
        >
            {
                isFail() && <Alert severity={"error"}>{"Fail to Sign in."}</Alert>
            }
            <Avatar sx={{m: 1, bgcolor: 'secondary.main'}}>
                <LockOutlined/>
            </Avatar>
            <Typography component="h1" variant="h5">
                Sign in
            </Typography>
            <Box
                component={'form'}
                onSubmit={onFinish}
                noValidate
                sx={{mt: 1}}
            >
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
                    control={<Checkbox value="remember" color="primary" disabled={state === State.pending}/>}
                    label="Remember me"
                />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        disabled={isPending()}
                        sx={{mt: 3, mb: 2, alignItems: 'center'}}
                    >
                        Sign In
                        {
                            isPending() &&
                            <CircularProgress
                                sx={{
                                    position: 'absolute'
                                }}
                            />
                        }
                    </Button>
            </Box>
        </Box>
    )
}


export function SignIn() {
    const token: string | null = useAppSelector(state => {
        return tokenSelector(state)
    })
    const isOn = token == null

    if (!isOn) {
        return (<></>)
    }


    return (
        <ThemeProvider theme={theme}>
            <Modal
                open={isOn}
                onClose={() => {
                }}
            >
                <Container component={'main'} maxWidth={'xs'} sx={{bgcolor: 'white'}}>
                    <CssBaseline/>
                    <SignInForm/>
                </Container>
            </Modal>
        </ThemeProvider>

    )
}