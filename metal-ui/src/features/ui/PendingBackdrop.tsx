
import { useCallback, useEffect, useRef, useState } from "react";

export interface PendingBackdropSize {
    width: number;
    height: number;
}

export interface PendingBackdropProps {
    isPending: boolean;
}

export const PendingBackdrop = (props: PendingBackdropProps) => {
    const { isPending } = props;
    const [size, setSize] = useState<PendingBackdropSize>({width: 0, height: 0});
    const containerRef = useRef<HTMLDivElement>(null);

    const syncSize = useCallback(() => {
        if (containerRef.current !== null && containerRef.current.parentElement !== null) {
            const height = containerRef.current.parentElement.clientHeight;
            const width = containerRef.current.parentElement.clientWidth;
            const newSize = {
                width: width === null ? 0 : width,
                height: height === null ? 0 : height,
            };
            setSize(newSize);
        }
    }, []);

    useEffect(() => {
        if (containerRef.current !== null && containerRef.current.parentElement !== null) {
            const observer = new ResizeObserver((entries) => {
                syncSize()
            })
            observer.observe(containerRef.current.parentElement)
            return ()=>{
                if (containerRef.current !== null && containerRef.current.parentElement !== null) {
                    observer.unobserve(containerRef.current.parentElement)
                    observer.disconnect();
                }
            }
        }
    }, [syncSize]);

    return (
        <div
            ref={containerRef}
            style={{
                position: "absolute",
                width: size.width.toString() + "px",
                height: size.height.toString() + "px",
                display: isPending ? "block" : "none",
                backgroundColor: "gray",
                opacity: "0.5",
            }}
        ></div>
    );
}
