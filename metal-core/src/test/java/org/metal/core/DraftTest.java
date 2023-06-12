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

package org.metal.core;

import org.metal.draft.Draft;

public class DraftTest {

    @Test
    public void case0() {
        Mock.MSourceImpl mSource =
                new Mock.MSourceImpl(
                        "00-00", "s-00", ImmutableMSourcePropsFoo.builder().schema("").build());
        Mock.MMapperImpl mapper =
                new Mock.MMapperImpl("10-00", "m-00", ImmutableMMapperPropsFoo.builder().build());

        Mock.MSinkImpl msink =
                new Mock.MSinkImpl("20-00", "sk-00", ImmutableMSinkPropsFoo.builder().build());

        Mock.MSinkImpl msink1 =
                new Mock.MSinkImpl("20-01", "sk-01", ImmutableMSinkPropsFoo.builder().build());

        Draft draft =
                Draft.builder()
                        .add(mSource)
                        .add(mapper)
                        .add(msink)
                        .add(msink1)
                        .addEdge(mSource, mapper)
                        .addEdge(mapper, msink)
                        .addEdge(mapper, msink1)
                        .withWait()
                        .waitFor(msink1, msink)
                        .build();
        System.out.println(draft);
    }
}
