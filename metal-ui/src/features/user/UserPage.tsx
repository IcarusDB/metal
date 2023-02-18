import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Alert,
    Button,
    Divider,
    LinearProgress,
    Typography,
} from "@mui/material";
import { IChangeEvent } from "@rjsf/core";
import { Form } from "@rjsf/mui";
import { RJSFSchema } from "@rjsf/utils";
import validator from "@rjsf/validator-ajv8";
import jwtDecode from "jwt-decode";
import { useCallback } from "react";
import { VscChevronDown } from "react-icons/vsc";
import { State } from "../../api/State";
import { updateName, UpdateNameRequest, UpdateNameResponse, updatePassword, UpdatePasswordRequest, UpdatePasswordResponse } from "../../api/UserApi";
import { useAppSelector } from "../../app/hooks";
import { MainHandler } from "../main/Main";
import { useUIAsync } from "../ui/UIHooks";
import { TokenUser } from "./UserBar";
import { tokenSelector } from "./userSlice";

export interface UserPageProps {
    mainHandler: MainHandler;
}

export function UserPage(props: UserPageProps) {
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });

    const user: TokenUser =
        token === null
            ? {
                  username: "Off Line",
                  _id: "*",
                  iat: -1,
              }
            : jwtDecode(token);

    return (
        <div
            style={{
                boxSizing: "border-box",
                paddingLeft: "1vw",
                paddingRight: "1vw",
                paddingTop: "1vh",
                paddingBottom: "1vh",
            }}
        >
            <Typography variant="h6" color={"text.secondary"}>
                {`User[${user.username}]`}
            </Typography>
            <UserNamePage token={token}/>
            <UserPasswordPage token={token} />
        </div>
    );
}

function useUpdateName(token: string | null): [(newName: string) => void, State] {
    const [run, status] = useUIAsync<UpdateNameResponse>();
    const update = useCallback(
        (newName: string) => {
            if (token === null) {
                return;
            }
            run(updateName(token, newName));
        },
        [run, token]
    );

    return [update, status];
}

interface UserNamePageProps {
    token: string | null;
}


function UserNamePage(props: UserNamePageProps) {
    const { token } = props;
    const [update, updateStatus] = useUpdateName(token);
    const formSchema: RJSFSchema = {
        type: "object",
        required: ["newName"],
        properties: {
            newName: {
                type: "string",
                title: "New Name",
            },
        },
    };

    const isPending = () => (updateStatus === State.pending);
    const isSuccess = () => (updateStatus === State.success);
    const isFailure = () => (updateStatus === State.failure);

    const onSubmit = (data: IChangeEvent<any, RJSFSchema, any>) => {
        const request: UpdateNameRequest = data.formData;
        update(request.newName);
    };

    return (
        <Accordion defaultExpanded={true}>
            <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
                <Typography variant="h5">User Name</Typography>
            </AccordionSummary>
            <AccordionDetails>
                {
                    isPending() && <LinearProgress />
                }
                <Divider orientation="horizontal" flexItem />
                { isSuccess() && <Alert severity="info" variant="outlined">{"Success to update name."}</Alert>}
                { isFailure() && <Alert severity="error" variant="outlined">{"Fail to update name."}</Alert>}
                <Form schema={formSchema} validator={validator} onSubmit={onSubmit} readonly={isPending()}>
                    <Button type={"submit"} variant={"contained"}>
                        {"Change"}
                    </Button>
                </Form>
            </AccordionDetails>
        </Accordion>
    );
}


function useUpdatePassword(token: string | null): [(oldPassword: string, newPassword: string) => void, State] {
    const [run, status] = useUIAsync<UpdatePasswordResponse>();
    const update = useCallback(
        (oldPassword: string, newPassword: string) => {
            if (token === null) {
                return;
            }
            run(updatePassword(token, oldPassword, newPassword));
        },
        [run, token]
    );

    return [update, status];
}

interface UserPasswordPageProps {
    token: string | null
}

function UserPasswordPage(props: UserPasswordPageProps) {
    const { token } = props;
    const [update, updateStatus] = useUpdatePassword(token);
    const formSchema: RJSFSchema = {
        type: "object",
        required: ["oldPassword", "newPassword"],
        properties: {
            oldPassword: {
                type: "string",
                title: "Old Password",
            },
            newPassword: {
                type: "string",
                title: "New Password",
            },
        },
    };

    const uiSchema = {
        oldPassword: {
            "ui:widget": "password"
        },
        newPassword: {
            "ui:widget": "password"
        }
    }

    const isPending = () => (updateStatus === State.pending);
    const isSuccess = () => (updateStatus === State.success);
    const isFailure = () => (updateStatus === State.failure);

    const onSubmit = (data: IChangeEvent<any, RJSFSchema, any>) => {
        const request: UpdatePasswordRequest = data.formData;
        update(request.oldPassword, request.newPassword);
    };

    return (
        <Accordion defaultExpanded={true}>
            <AccordionSummary expandIcon={<VscChevronDown size={"2em"} />}>
                <Typography variant="h5">User Password</Typography>
            </AccordionSummary>
            <AccordionDetails>
                {
                    isPending() && <LinearProgress />
                }
                <Divider orientation="horizontal" flexItem />
                { isSuccess() && <Alert severity="info" variant="outlined">{"Success to update password."}</Alert>}
                { isFailure() && <Alert severity="error" variant="outlined">{"Fail to update password."}</Alert>}
                <Form 
                    schema={formSchema} 
                    uiSchema={uiSchema}
                    validator={validator} 
                    onSubmit={onSubmit} 
                    readonly={isPending()}>
                    <Button type={"submit"} variant={"contained"}>
                        {"Change"}
                    </Button>
                </Form>
            </AccordionDetails>
        </Accordion>
    );
}