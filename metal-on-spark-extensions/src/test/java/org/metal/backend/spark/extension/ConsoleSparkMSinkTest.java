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

package org.metal.backend.spark.extension;

import org.metal.backend.spark.SparkMetalService;
import org.metal.backend.spark.SparkTranslator;
import org.metal.core.Pair;
import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import org.apache.spark.sql.SparkSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ConsoleSparkMSinkTest {

    @Test
    public void testConsoleSink() throws JsonProcessingException {
        String path = "src/test/resources/test.json";
        IJsonFileMSourceProps sourceProps =
                ImmutableIJsonFileMSourceProps.builder().path(path).schema("").build();
        System.out.println(sourceProps);
        JsonFileMSource source = new JsonFileMSource("00-00", "source-00", sourceProps);

        IConsoleMSinkProps sinkProps = ImmutableIConsoleMSinkProps.builder().numRows(10).build();
        ConsoleMSink sink = new ConsoleMSink("01-00", "sink-00", sinkProps);
        System.out.println(sinkProps);

        System.out.println(source.props());
        System.out.println(sink.props());

        Spec spec = new Spec("1.0");
        spec.getMetals().add(source);
        spec.getMetals().add(sink);
        spec.getEdges().add(Pair.of(source.id(), sink.id()));

        Draft draft = DraftMaster.draft(spec);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));

        SparkSession platform =
                SparkSession.builder().appName("test").master("local[*]").getOrCreate();

        SparkTranslator forgeMaster = new SparkTranslator(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(forgeMaster);
        service.analyse(draft);
        service.exec();
    }

    private String json =
            "{\n"
                    + "  \"version\" : \"1.0\",\n"
                    + "  \"metals\" : [ {\n"
                    + "    \"type\" : \"org.metal.backend.spark.extension.JsonFileMSource\",\n"
                    + "    \"id\" : \"00-00\",\n"
                    + "    \"name\" : \"source-00\",\n"
                    + "    \"props\" : {\n"
                    + "      \"schema\" : \"\",\n"
                    + "      \"path\" : \"src/test/resources/test.json\"\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"type\" : \"org.metal.backend.spark.extension.ConsoleMSink\",\n"
                    + "    \"id\" : \"01-00\",\n"
                    + "    \"name\" : \"sink-00\",\n"
                    + "    \"props\" : {\n"
                    + "      \"numRows\" : 10\n"
                    + "    }\n"
                    + "  } ],\n"
                    + "  \"edges\" : [ {\n"
                    + "    \"left\" : \"00-00\",\n"
                    + "    \"right\" : \"01-00\"\n"
                    + "  } ]\n"
                    + "}";

    @Test
    public void testConsoleSinkJson() throws IOException {
        Spec spec = new SpecFactoryOnJson().get(json);
        Draft draft = DraftMaster.draft(spec);

        SparkSession platform =
                SparkSession.builder().appName("test").master("local[*]").getOrCreate();
        SparkMetalService<IMetalProps> service =
                SparkMetalService.<IMetalProps>of(new SparkTranslator(platform));

        service.analyse(draft);
        System.out.println(service.analysed());
        System.out.println(service.unAnalysed());
        service.exec();
    }
}
