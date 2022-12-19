import { Logout } from "@mui/icons-material";
import { Avatar, Divider, IconButton, ListItem, ListItemIcon, Menu, MenuItem, Typography } from "@mui/material";
import { height } from "@mui/system";
import jwtDecode from "jwt-decode";
import { useState } from "react";
import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { logout, tokenSelector } from "./userSlice";

interface TokenUser {
    username: string,
    _id: string,
    iat: number,
}

export interface UserBarProps {

}

export function UserBar(props: UserBarProps) {
    const dispatch = useAppDispatch();
    const token: string | null = useAppSelector((state) => {
        return tokenSelector(state);
    });
    
    const user: TokenUser = token === null? {
        username: "Off Line",
        _id: "*",
        iat: -1,
    }: jwtDecode(token);

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
                <Divider orientation="vertical" />
                <Typography variant="h6" sx={{ 
                        width: "100%", 
                        overflow: "hidden", 
                        textOverflow: "ellipsis",
                        paddingLeft: "1vw",
                        paddingRight: "1vw",
                    }}
                >
                    {user.username}
                </Typography>
                <IconButton onClick={onOpenMenu}>
                    <Avatar alt="user" src="/images/metal.png" />
                </IconButton>
            </div>
            <Menu 
                open={isOpenMenu}
                onClose={onCloseMenu}
                onClick={onCloseMenu}
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