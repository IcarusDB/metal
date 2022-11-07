import './UserNotice.css';
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {tokenSelector} from "./userSlice";


import {useEffect} from "react";

export function UserNotice() {
    const token: string | null = useAppSelector(state => {return tokenSelector(state)})
    const dispatch = useAppDispatch();

    useEffect(()=>{
        if (token != null) {
            // message.info("User has authenticated.")
        } else {
            // message.info("User has no authenticated.")
        }
    }, [token])

    return (<></>)
}
