/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import { ReactNode } from "react";

export interface PendingBackdropSize {
  width: number;
  height: number;
}

export interface PendingBackdropProps {
  open: boolean;
  children?: ReactNode;
  backgroundColor?: string;
  opacity?: string;
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
        backgroundColor:
          backgroundColor === undefined ? "gray" : backgroundColor,
        opacity: opacity === undefined ? "0.5" : opacity,
      }}
    >
      {children}
    </div>
  );
};
