import { Logout } from "@mui/icons-material";
import { Avatar, Divider, IconButton, ListItemIcon, Menu, MenuItem, Typography } from "@mui/material";
import { useState } from "react";
import { useAppDispatch } from "../../app/hooks";
import { Notice } from "../notice/Notice";
import { logout } from "./userSlice";
import { useUser } from "./UserStore";

export interface TokenUser {
    username: string,
    _id: string,
    iat: number,
}

export interface UserBarProps {

}

export function UserBar(props: UserBarProps) {
    const dispatch = useAppDispatch();
    const {user} = useUser();

    const [anchor, setAnchor] = useState<null | HTMLElement>(null);
    const isOpenMenu = Boolean(anchor);

    const onOpenMenu = (event: React.MouseEvent<HTMLElement>) => {
        setAnchor(event.currentTarget);
    }

    const onCloseMenu = () => {
        setAnchor(null);
    }

    const onLogout = () => {
        dispatch(logout(undefined));
    }
    
    return (
        <div
            style={{
                display: "flex",
                flexDirection: "row",
                alignItems: "center",
                justifyContent: "space-between",
                height: "100%",
                width: "100%",
                boxSizing: "border-box",
                paddingLeft: "1vw",
                paddingRight: "1vw",
                paddingTop: "1vh",
                paddingBottom: "1vh",
            }}
        > 
            <img src="/images/metal_brand.png" alt="brand" height={"100%"}/>
            <div
                style={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "flex-end",
                    height: "100%",
                }}
            >
                <Notice />
                <Divider orientation="vertical" />
                <Typography variant="h6" sx={{ 
                        width: "100%", 
                        overflow: "hidden", 
                        textOverflow: "ellipsis",
                        paddingLeft: "1vw",
                        paddingRight: "1vw",
                    }}
                >
                    {user.name}
                </Typography>
                <IconButton onClick={onOpenMenu}>
                    <Avatar alt="user" src="/images/metal.png" />
                </IconButton>
            </div>
            <Menu 
                open={isOpenMenu}
                onClick={onCloseMenu}
                onClose={onCloseMenu}
                anchorEl={anchor}
            >
                <MenuItem onClick={onLogout}>
                    <ListItemIcon>
                        <Logout />
                    </ListItemIcon>
                    Logout
                </MenuItem>

            </Menu>
            
        </div>
    )
}