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

import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactory;
import org.metal.specs.SpecFactoryOnJson;
import org.metal.translator.Translator;

import com.google.common.collect.HashMultimap;
import com.google.common.hash.HashCode;

import java.io.IOException;
import java.util.HashMap;

public class TranslatorTest {

    private String json =
            "{\n"
                    + "  \"version\" : \"0.0.1\",\n"
                    + "  \"metals\" : [ {\n"
                    + "    \"type\" : \"org.metal.core.Mock$MSourceImpl\",\n"
                    + "    \"id\" : \"00-00\",\n"
                    + "    \"name\" : \"source-00\",\n"
                    + "    \"props\" : {\n"
                    + "      \"schema\" : \"{}\"\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"type\" : \"org.metal.core.Mock$MMapperImpl\",\n"
                    + "    \"id\" : \"01-00\",\n"
                    + "    \"name\" : \"mapper-00\",\n"
                    + "    \"props\" : { }\n"
                    + "  }, {\n"
                    + "    \"type\" : \"org.metal.core.Mock$MMapperImpl\",\n"
                    + "    \"id\" : \"01-01\",\n"
                    + "    \"name\" : \"mapper-01\",\n"
                    + "    \"props\" : { }\n"
                    + "  }, {\n"
                    + "    \"type\" : \"org.metal.core.Mock$MFusionImpl\",\n"
                    + "    \"id\" : \"02-00\",\n"
                    + "    \"name\" : \"fusion-00\",\n"
                    + "    \"props\" : { }\n"
                    + "  }, {\n"
                    + "    \"type\" : \"org.metal.core.Mock$MSinkImpl\",\n"
                    + "    \"id\" : \"03-00\",\n"
                    + "    \"name\" : \"sink-00\",\n"
                    + "    \"props\" : { }\n"
                    + "  } ],\n"
                    + "  \"edges\" : [ {\n"
                    + "    \"left\" : \"00-00\",\n"
                    + "    \"right\" : \"01-00\"\n"
                    + "  }, {\n"
                    + "    \"left\" : \"00-00\",\n"
                    + "    \"right\" : \"01-01\"\n"
                    + "  }, {\n"
                    + "    \"left\" : \"01-00\",\n"
                    + "    \"right\" : \"02-00\"\n"
                    + "  }, {\n"
                    + "    \"left\" : \"01-01\",\n"
                    + "    \"right\" : \"02-00\"\n"
                    + "  }, {\n"
                    + "    \"left\" : \"02-00\",\n"
                    + "    \"right\" : \"03-00\"\n"
                    + "  } ]\n"
                    + "}";

    @Test
    public void testForge() throws IOException {
        SpecFactory specFactory = new SpecFactoryOnJson();
        Spec spec = specFactory.get(json);
        Draft draft = DraftMaster.draft(spec);
        Translator master = new Translator(Thread.currentThread());
        master.translate(draft);
        HashMap<HashCode, IMExecutor> mProducts = master.context().mProducts();
        mProducts.forEach(
                (HashCode hashCode, IMExecutor mProduct) -> {
                    System.out.println(hashCode.toString());
                    mProduct.exec();
                });

        HashMap<HashCode, String> dfs = master.context().dfs();
        dfs.forEach(
                (HashCode hashCode, String df) -> {
                    System.out.println(hashCode);
                    System.out.println(df);
                });

        HashMultimap<HashCode, Metal<String, Thread, IMetalProps>> hash2metal =
                master.context().hash2metal();
        hash2metal.forEach(
                (hashcode, metal) -> {
                    System.out.println(hashcode);
                    System.out.println(metal);
                });

        HashMap<Metal<String, Thread, IMetalProps>, HashCode> metal2hash =
                master.context().metal2hash();
        metal2hash.forEach(
                (metal, hash) -> {
                    System.out.println(metal);
                    System.out.println(hash);
                });
    }
}
