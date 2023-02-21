
import { ReactNode, useCallback, useEffect, useRef, useState } from "react";

export interface PendingBackdropSize {
    width: number;
    height: number;
}

export interface PendingBackdropProps {
    open: boolean,
    children?: ReactNode,
    backgroundColor?: string,
    opacity?: string,
}

export const ResizeBackdrop = (props: PendingBackdropProps) => {
    const { open, children, backgroundColor, opacity } = props;

    return (
        <div
            style={{
                boxSizing: "border-box",
                position: "absolute",
                overflow: "hidden",
                top: "0px",
                bottom: "0px",
                left: "0px",
                right: "0px",
                display: open ? "block" : "none",
                backgroundColor: backgroundColor === undefined? "gray": backgroundColor,
                opacity: opacity === undefined? "0.5": opacity,
            }}
        >
            {children}
        </div>
    );
}
