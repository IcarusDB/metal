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


import { Alert, LinearProgress } from "@mui/material";
import { useEffect } from "react";
import { getExecOfId } from "../../api/ExecApi";
import { useAsync } from "../../api/Hooks";
import { State } from "../../api/State";
import { Exec } from "../../model/Exec";
import {
  useBackendArgsFn,
  useNameFn,
  usePkgsFn,
  usePlatformFn,
  useSpecFn,
} from "../designer/DesignerProvider";
import { MainHandler } from "../main/Main";

function useExecLoader(
  token: string | null,
  id: string,
  name?: string
): [State, any] {
  const [, setSpec] = useSpecFn();
  const [, setName] = useNameFn();
  const [, setPkgs] = usePkgsFn();
  const [, setPlatform] = usePlatformFn();
  const [, setBackendArgs] = useBackendArgsFn();
  const [run, status, exec, error] = useAsync<Exec>();

  useEffect(() => {
    if (token === null || id.trim() === "") {
      return;
    }
    if (status === State.idle) {
      run(getExecOfId(token, id));
    }

    if (status === State.success && exec !== null) {
      setSpec(exec.SPEC);
      setName(
        name !== undefined
          ? `Project[${name}]-${exec.id}`
          : `Project[${exec.fromProject}]-${exec.id}`
      );
      setPkgs(exec.deploy.pkgs);
      setPlatform(exec.deploy.platform);
      setBackendArgs(exec.deploy.backend.args);
    }
  }, [
    exec,
    id,
    name,
    run,
    setBackendArgs,
    setName,
    setPkgs,
    setPlatform,
    setSpec,
    status,
    token,
  ]);

  return [status, error];
}

export interface ExecLoaderProps {
  id: string;
  token: string | null;
  mainHandler?: MainHandler;
  name?: string;
}

export function ExecLoader(props: ExecLoaderProps) {
  const { id, token, mainHandler, name } = props;
  const [loadStatus, loadError] = useExecLoader(token, id, name);
  const isPending = () => loadStatus === State.pending;
  const isFailure = () => loadStatus === State.failure;

  return (
    <div>
      {isPending() && <LinearProgress />}
      {isFailure() && <Alert severity={"error"}>{"Fail to load exec."}</Alert>}
    </div>
  );
}
