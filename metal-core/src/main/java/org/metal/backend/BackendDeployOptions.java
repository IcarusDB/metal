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

package org.metal.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendDeployOptions<S> {

    @JsonProperty("confs")
    private Map<String, Object> confs;

    @JsonProperty("setups")
    private List<ISetup<S>> setups;

    @JsonCreator
    public BackendDeployOptions() {
        confs = new HashMap<>();
        setups = new ArrayList<>();
    }

    public Map<String, Object> getConfs() {
        return confs;
    }

    public List<ISetup<S>> getSetups() {
        return setups;
    }

    @Override
    public String toString() {
        return "BackendDeployOptions{" + "confs=" + confs + ", setups=" + setups + '}';
    }
}
