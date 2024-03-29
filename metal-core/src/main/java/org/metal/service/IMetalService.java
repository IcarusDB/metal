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

package org.metal.service;

import org.metal.core.Metal;
import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;

import org.apache.arrow.vector.types.pojo.Schema;

import java.util.List;
import java.util.NoSuchElementException;

public interface IMetalService<D, S, P extends IMetalProps> {

    public D df(String id) throws NoSuchElementException;

    public Metal<D, S, P> metal(String id) throws NoSuchElementException;

    public List<String> analysed();

    public List<String> unAnalysed();

    public void analyse(Draft draft) throws MetalAnalysedException, IllegalStateException;

    public void exec() throws MetalExecuteException;

    public Schema schema(String id) throws MetalServiceException;
}
