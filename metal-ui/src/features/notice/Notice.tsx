import {
    Alert,
    Divider,
    IconButton,
    List,
    ListItem,
    Popover,
    Typography,
} from "@mui/material";
import moment from "moment";
import { useEffect, useState } from "react";
import { VscBell, VscBellDot, VscClearAll } from "react-icons/vsc";
import create from "zustand";
import { subscribeWithSelector } from "zustand/middleware";

export interface Message {
    content: string | JSX.Element;
    time: number;
}

export interface Logger {
    info: (message: string | JSX.Element) => void;
    warning: (message: string | JSX.Element) => void;
    success: (message: string | JSX.Element) => void;
    error: (message: string | JSX.Element) => void;
}

interface Store {
    messages: Message[];
}

export interface NoticeAction {
    put: (message: string | JSX.Element) => void;
    clear: () => void;
}

declare type NoticeStore = Store & NoticeAction & Logger;

const MAX_SIZE = 64;

function combinePrevMessages(messages: Message[], message: Message) {
    const combineMsgs = [message, ...messages];
    combineMsgs.splice(MAX_SIZE);
    return combineMsgs;
}

export function loggerSelector(store: NoticeStore): Logger {
    return {
        info: store.info,
        error: store.error,
        success: store.success,
        warning: store.warning,
    }
}

export const useNotice = create<NoticeStore>()(
    subscribeWithSelector((set, get) => ({
        messages: [],
        put: (message: string | JSX.Element) => {
            set((prev) => ({
                messages: combinePrevMessages(
                    prev.messages, {
                        content: message,
                        time: new Date().getTime(),
                    }
                )
            }));
        },
        info: (message: string | JSX.Element) => {
            const wrap = (
                <Alert severity="info" variant="outlined">
                    {message}
                </Alert>
            );
            set((prev) => ({
                messages: combinePrevMessages(
                    prev.messages, {
                        content: wrap,
                        time: new Date().getTime(),
                    }
                )
            }));
        },
        warning: (message: string | JSX.Element) => {
            const wrap = (
                <Alert severity="warning" variant="outlined">
                    {message}
                </Alert>
            );
            set((prev) => ({
                messages: combinePrevMessages(
                    prev.messages, {
                        content: wrap,
                        time: new Date().getTime(),
                    }
                )
            }));
        },
        success: (message: string | JSX.Element) => {
            const wrap = (
                <Alert severity="success" variant="outlined">
                    {message}
                </Alert>
            )
            set((prev) => ({
                messages: combinePrevMessages(
                    prev.messages, {
                        content: wrap,
                        time: new Date().getTime(),
                    }
                )
            }));
        },
        error: (message: string | JSX.Element) => {
            const wrap = (
                <Alert severity="error" variant="outlined">
                    {message}
                </Alert>
            );
            set((prev) => ({
                messages: combinePrevMessages(
                    prev.messages, {
                        content: wrap,
                        time: new Date().getTime(),
                    }
                )
            }));
        },
        clear: () => {
            set(() => ({
                messages: [],
            }));
        },
    }))
);


export function Notice() {
    const [messages, clear] = useNotice((state) => [state.messages, state.clear]);
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);
    const [isNew, setIsNew] = useState(false);

    const onClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchor(event.currentTarget);
        setIsNew(false);
    };

    const onClose = () => {
        setAnchor(null);
    };

    const onClear = () => {
        clear();
    };

    const isOpen = anchor !== null;

    useEffect(()=>{
        return useNotice.subscribe(
            (state) => state.messages,
            (state, prev) => {
                if (state.length >= prev.length) {
                    setIsNew(true);
                }
            }
        );
    }, []);

    return (
        <>
            <IconButton onClick={onClick}>{isNew ? <VscBellDot /> : <VscBell />}</IconButton>
            <Popover
                open={isOpen}
                anchorEl={anchor}
                onClose={onClose}
                anchorOrigin={{
                    vertical: "bottom",
                    horizontal: "right",
                }}
                transformOrigin={{
                    vertical: "top",
                    horizontal: "right",
                }}
            >
                <div
                    style={{
                        overflowY: "hidden",
                        maxWidth: "40vw",
                    }}
                >
                    <div
                        style={{
                            boxSizing: "border-box",
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                            minWidth: "10vw",
                        }}
                    >
                        <IconButton onClick={onClear}>
                            <VscClearAll />
                        </IconButton>
                    </div>
                    <Divider flexItem orientation="horizontal" />
                    <List
                        sx={{
                            boxSizing: "border-box",
                            paddingLeft: "1vw",
                            paddingRight: "1vw",
                            paddingBottom: "1vh",
                            overflowY: "scroll",
                            minWidth: "20vw",
                            maxHeight: "40vh",
                        }}
                    >
                        {messages.length === 0 && (
                            <Alert severity="info" variant="outlined">
                                Empty
                            </Alert>
                        )}
                        {messages.length > 0 &&
                            messages.map((msg, idx) => (
                                <ListItem key={idx}>
                                    <div
                                        style={{
                                            display: "flex",
                                            flexDirection: "column",
                                            alignItems: "flex-start",
                                            justifyContent: "flex-start",
                                            width: "100%",
                                            height: "100%",
                                        }}
                                    >
                                        {msg.content}
                                        <div
                                            style={{
                                                display: "flex",
                                                flexDirection: "row",
                                                alignItems: "center",
                                                justifyContent: "flex-end",
                                                width: "100%",
                                                height: "100%",
                                            }}
                                        >
                                            <Typography color={"text.secondary"} variant="caption">
                                                {moment(msg.time).format("YYYY-MM-DD HH:mm:ss")}
                                            </Typography>
                                        </div>
                                        <Divider flexItem />
                                    </div>
                                </ListItem>
                            ))}
                    </List>
                </div>
            </Popover>
        </>
    );
}
