import { Alert, Divider, IconButton, List, ListItem, Popover, Typography } from "@mui/material";
import moment from "moment";
import { useEffect, useState } from "react";
import { VscBell, VscBellDot, VscClearAll } from "react-icons/vsc";
import { useMessages, useMessagesAction } from "../DesignerProvider";


export function BackendNotice() {
    const [messages,, onMessagesChange] = useMessages();
    const {clear} = useMessagesAction();
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
        onMessagesChange((msgs, prev) => {
            if (msgs === undefined) {
                setIsNew(false);
                return;
            }

            if (prev === undefined) {
                setIsNew(true);
                return;
            }

            if (msgs?.length >= prev?.length) {
                setIsNew(true);
            }
        })
    }, [onMessagesChange]);
    
    return (
<>
            <IconButton onClick={onClick}>{isNew ? <VscBellDot /> : <VscBell />}</IconButton>
            <Popover
                open={isOpen}
                anchorEl={anchor}
                onClose={onClose}
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                  }}
                transformOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
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
