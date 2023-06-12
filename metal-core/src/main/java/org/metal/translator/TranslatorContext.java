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

package org.metal.translator;

import org.metal.core.IMExecutor;
import org.metal.core.Metal;
import org.metal.draft.Draft;

import org.immutables.value.Value;

import com.google.common.collect.HashMultimap;
import com.google.common.hash.HashCode;

import java.util.HashMap;

@Value.Immutable
public interface TranslatorContext<D, S> {

    public Draft draft();

    public HashMap<HashCode, D> dfs();

    public HashMap<Metal, HashCode> metal2hash();

    public HashMultimap<HashCode, Metal> hash2metal();

    public HashMap<HashCode, IMExecutor> mProducts();

    public HashMap<String, Metal> id2metal();
}
