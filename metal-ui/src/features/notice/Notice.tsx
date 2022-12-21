import { Alert, Divider, IconButton, List, ListItem, Paper, Popover } from "@mui/material";
import { useState } from "react";
import { VscBell, VscClearAll } from "react-icons/vsc";
import create from "zustand";

interface NoticeStore {
    messages: string[],
    put: (message: string) => void,
}

export const useNotice = create<NoticeStore>()((set) => ({
    messages: [],
    put: (message: string) => {
        set((prev) => ({messages: [message, ...prev.messages]}))
    }
}));


export function Notice() {
    const messages = useNotice((state) => (state.messages));
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);
    const onClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchor(event.currentTarget);
    }

    const onClose = () => {
        setAnchor(null);
    }

    const isOpen = anchor !== null;

    return (
        <>
        <IconButton
            onClick={onClick}
        >
            <VscBell />
        </IconButton>
        <Popover
            open={isOpen}
            anchorEl={anchor}
            onClose={onClose}
            anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'right',
              }}
            transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
            }}
            sx={{
                maxWidth: "40vw",
                maxHeight: "40vh",
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
                <IconButton>
                    <VscClearAll />
                </IconButton>
            </div>
            <Divider flexItem orientation="horizontal"/>
            <List sx={{
                overflow: "scroll",
                minWidth: "20vw",
            }}>
                {
                    messages.map(msg => (
                        <ListItem>
                            <Alert severity="info" variant="outlined" >
                                {msg}
                            </Alert>
                        </ListItem>
                    ))
                }
            </List>
        </Popover>
        </>
        
    )
}