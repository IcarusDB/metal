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


import Ajv, { JSONSchemaType } from "ajv";

export interface MetalPkg {
  id: string;
  userId: string;
  type: string;
  scope: "PRIVATE" | "PUBLIC";
  createTime: number;
  pkg: string;
  class: string;
  groupId: string;
  artifactId: string;
  version: string;
  formSchema: any;
  uiSchema?: any;
  description?: any;
}

export enum Scope {
  PUBLIC = "PUBLIC",
  PRIVATE = "PRIVATE",
}

interface MetalPkgBasic {
  pkg: string;
  class: string;
  groupId?: string;
  artifactId?: string;
  version?: string;
  formSchema: any;
  uiSchema?: any;
  description?: any;
}

export interface MetalManifest {
  sources?: MetalPkgBasic[];
  sinks?: MetalPkgBasic[];
  mappers?: MetalPkgBasic[];
  fusions?: MetalPkgBasic[];
  setups?: MetalPkgBasic[];
}

const metalPkgBasicSchema: JSONSchemaType<MetalPkgBasic> = {
  type: "object",
  properties: {
    pkg: {
      type: "string",
    },
    class: {
      type: "string",
    },
    groupId: {
      type: "string",
      nullable: true,
    },
    artifactId: {
      type: "string",
      nullable: true,
    },
    version: {
      type: "string",
      nullable: true,
    },
    formSchema: {
      type: "object",
      nullable: true,
    },
    uiSchema: {
      type: "object",
      nullable: true,
    },
    description: {
      type: "object",
      nullable: true,
    },
  },
  required: ["pkg", "class"],
  additionalProperties: false,
};

const manifestSchema: JSONSchemaType<MetalManifest> = {
  type: "object",
  properties: {
    sources: {
      type: "array",
      items: metalPkgBasicSchema,
      nullable: true,
    },
    sinks: {
      type: "array",
      items: metalPkgBasicSchema,
      nullable: true,
    },
    mappers: {
      type: "array",
      items: metalPkgBasicSchema,
      nullable: true,
    },
    fusions: {
      type: "array",
      items: metalPkgBasicSchema,
      nullable: true,
    },
    setups: {
      type: "array",
      items: metalPkgBasicSchema,
      nullable: true,
    },
  },
  required: [],
  additionalProperties: false,
};

const ajv = new Ajv();
const validate = ajv.compile(manifestSchema);

export function validMainfest(manifest: unknown) {
  return validate(manifest);
}
