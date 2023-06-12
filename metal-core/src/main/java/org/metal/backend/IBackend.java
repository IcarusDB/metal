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

import org.metal.core.props.IMetalProps;
import org.metal.service.BaseMetalService;

public interface IBackend<D, S, P extends IMetalProps> {

    public void start() throws IllegalArgumentException;

    public void stop();

    public <R extends BaseMetalService<D, S, P>> R service() throws IllegalArgumentException;

    public static interface IBuilder<D, S, P extends IMetalProps> {

        public IBuilder conf(String key, Object value);

        public IBuilder setup(ISetup<S> setup);

        public IBuilder deployOptions(BackendDeployOptions<S> options);

        public IBackend<D, S, P> build();
    }
}
