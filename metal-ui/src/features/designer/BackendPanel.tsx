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

import { Chip, Divider, Grid, IconButton, Paper, Skeleton, Typography } from "@mui/material";
import { AxiosError } from "axios";
import moment from "moment";
import {
    ForwardedRef,
    forwardRef,
    useCallback,
    useEffect,
    useImperativeHandle,
    useState,
} from "react";
import { VscDebugCoverage, VscDebugStart, VscVmConnect, VscVmOutline } from "react-icons/vsc";
import { ApiResponse, ApiResponseEntity } from "../../api/APIs";
import { useAsync } from "../../api/Hooks";
import { deployBackendOfId, DeployResponse, getBackendStatus, getDeploy, undeployBackendOfId, UnDeployResponse } from "../../api/ProjectApi";
import { useAppSelector } from "../../app/hooks";
import { BackendState, BackendStatus, Deploy } from "../../model/Project";
import { Spec } from "../../model/Spec";
import { tokenSelector } from "../user/userSlice";

export function backendStatusColor(status: BackendState) {
    switch (status) {
        case BackendState.CREATED:
            return "primary";
        case BackendState.UP:
            return "success";
        case BackendState.DOWN:
            return "info";
        case BackendState.FAILURE:
            return "error";
    }
}
export interface BackendStatusViewProps {
    status?: BackendStatus;
}

export function BackendStatusView(props: BackendStatusViewProps) {
    const { status } = props;
    if (status === undefined) {
        return <Chip label="No Backend" variant="outlined" color="default" />;
    }
    const color = backendStatusColor(status.current);
    return <Chip label={status.current} variant="filled" color={color} />;
}

export interface BackendPanelProps {
    deployId: string;
    currentSpec: () => Spec;
}

export interface BackendPanelHandler {
    load: () => void;
}

export const BackendPanel = forwardRef(
    (props: BackendPanelProps, ref: ForwardedRef<BackendPanelHandler>) => {
        const token: string | null = useAppSelector((state) => {
            return tokenSelector(state);
        });
        const { deployId, currentSpec } = props;
        // const [fetchDeploy, fetchDeployStatus, deploy, fetchDeployError] = useAsync<Deploy>();
        const [fetchBackendStatus, fetchBackendStatusStatus, backendStatus, fetchBackendStatusError] = useAsync<BackendStatus>();
        const [deployBackend, deployBackendStatus, deployResult, deployBackendError] = useAsync<DeployResponse>();
        const [undeployBackend, undeployBackendStatus, undeployResult, undeployBackendError] = useAsync<UnDeployResponse>();

        const error = useCallback(() => {
            if (fetchBackendStatusError !== null) {
                const backendStatusError: AxiosError<ApiResponseEntity> = fetchBackendStatusError;
                const apiResponseError = ApiResponse.extractErrorMessage(backendStatusError);
                if (apiResponseError === undefined) {
                    return "Fail to fetch backend status.";
                }
                return apiResponseError;
                
            }
            if (deployBackendError !== null) {
                const deployError: AxiosError<ApiResponseEntity> = deployBackendError;
                const apiResponseError = ApiResponse.extractErrorMessage(deployError);
                if (apiResponseError === undefined) {
                    return "Fail to deploy backend status.";
                }
                return apiResponseError;
            }
            if (undeployBackendError !== null) {
                const undeployError: AxiosError<ApiResponseEntity> = undeployBackendError;
                const apiResponseError = ApiResponse.extractErrorMessage(undeployError);
                if (apiResponseError === undefined) {
                    return "Fail to undeploy backend status.";
                }
                return apiResponseError;
            }


        }, [deployBackendError, fetchBackendStatusError, undeployBackendError]);
        
        const isDeploySuccess = useCallback(()=>{
            if (backendStatus === null) {
                return false;
            }
            if (backendStatus.current === BackendState.DOWN || backendStatus.current === BackendState.FAILURE) {
                return false;
            }
            return true;
        }, [backendStatus]);

        const onLoadBackendStatus = useCallback(() => {
            if (token === null) {
                return ;
            }
            fetchBackendStatus(getBackendStatus(token, deployId));
        }, [deployId, fetchBackendStatus, token])

        const onDeploy = useCallback(() => {
            if (token === null) {
                return;
            }
            deployBackend(deployBackendOfId(token, deployId));
        }, [deployBackend, deployId, token])

        const onUndeploy = useCallback(() => {
            if (token === null) {
                return;
            }
            undeployBackend(undeployBackendOfId(token, deployId));
        }, [deployId, token, undeployBackend]);

        useImperativeHandle(
            ref,
            () => ({
                load: onLoadBackendStatus,
            }),
            [onLoadBackendStatus]
        );

        useEffect(() => {
            console.log("load status.");
            onLoadBackendStatus();
            if (deployResult?.success || backendStatus?.current === BackendState.CREATED || backendStatus?.current === BackendState.UP) {
                const timer = setTimeout(onLoadBackendStatus, 3000);
                return () => {
                    clearTimeout(timer);
                }
            }
        }, [backendStatus, deployResult?.success, onLoadBackendStatus]);

        return (
            <Paper
                square
                variant="outlined"
                sx={{
                    minWidth: "15em",
                    maxWidth: "20em",
                    minHeight: "20em",
                }}
            >
                <Grid container spacing={1}>
                    <Grid
                        item
                        xs={6}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <IconButton 
                            disabled={isDeploySuccess()}
                            onClick={()=>{onDeploy()}}
                        >
                            <VscVmConnect />
                        </IconButton>
                        <IconButton
                            onClick={()=>{onUndeploy()}}
                            >
                            <VscVmOutline />
                        </IconButton>
                    </Grid>
                    <Grid
                        item
                        xs={6}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Divider variant="middle" orientation="vertical" flexItem />
                        <IconButton onClick={()=>{console.log(currentSpec())}}>
                            <VscDebugCoverage />
                        </IconButton>
                        <IconButton>
                            <VscDebugStart />
                        </IconButton>
                    </Grid>
                    <Grid item xs={12}>
                        <Divider />
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "flex-start",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Deploy ID"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "flex-start",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Typography>{deployId}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Epoch"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Typography>{backendStatus?.epoch}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Status"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <BackendStatusView status={backendStatus === null? undefined: backendStatus} />
                    </Grid>

                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "flex-start",
                            justifyContent: "flex-end",
                        }}
                    >
                        <Typography>{"Message"}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        <Typography>{backendStatus?.failureMsg || error()}</Typography>
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {backendStatus?.upTime !== undefined && (
                            <Typography>{"Up Time"}</Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {backendStatus?.upTime !== undefined && (
                            <Typography>
                                {moment(backendStatus.upTime).format("YYYY-MM-DD HH:mm:ss")}
                            </Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {backendStatus?.downTime !== undefined && (
                            <Typography>{"Down Time"}</Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {backendStatus?.downTime !== undefined && (
                            <Typography>
                                {moment(backendStatus.downTime).format(
                                    "YYYY-MM-DD HH:mm:ss"
                                )}
                            </Typography>
                        )}
                    </Grid>
                    <Grid
                        item
                        xs={4}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-end",
                        }}
                    >
                        {backendStatus?.failureTime !== undefined &&
                            backendStatus.current === BackendState.FAILURE && (
                                <Typography>{"Failure Time"}</Typography>
                            )}
                    </Grid>
                    <Grid
                        item
                        xs={8}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "flex-start",
                        }}
                    >
                        {backendStatus?.failureTime !== undefined &&
                            backendStatus.current === BackendState.FAILURE && (
                                <Typography>
                                    {moment(backendStatus?.failureTime).format(
                                        "YYYY-MM-DD HH:mm:ss"
                                    )}
                                </Typography>
                            )}
                    </Grid>
                </Grid>
            </Paper>
        );
    }
);
