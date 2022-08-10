package org.metal.backend.spark.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.metal.backend.spark.SparkForgeMaster;
import org.metal.backend.spark.SparkMetalService;
import org.metal.core.Pair;
import org.metal.core.draft.Draft;
import org.metal.core.draft.DraftMaster;
import org.metal.core.props.IMetalProps;
import org.metal.core.specs.Spec;
import org.metal.core.specs.SpecFactoryOnJson;

import java.io.IOException;

public class ConsoleSparkMSinkTest {
    @Test
    public void testConsoleSink() throws JsonProcessingException {
        String path = "src/test/resources/test.json";
        IJsonFileMSourceProps sourceProps = ImmutableIJsonFileMSourceProps.builder()
                .path(path)
                .schema("")
                .build();
        System.out.println(sourceProps);
        JsonFileMSource source = new JsonFileMSource(
                "00-00",
                "source-00",
                sourceProps
        );

        IConsoleMSinkProps sinkProps = ImmutableIConsoleMSinkProps.builder()
                .numRows(10)
                .build();
        ConsoleMSink sink = new ConsoleMSink(
                "01-00",
                "sink-00",
                sinkProps
        );
        System.out.println(sinkProps);

        System.out.println(source.props());
        System.out.println(sink.props());


        Spec spec = new Spec("1.0");
        spec.getMetals().add(source);
        spec.getMetals().add(sink);
        spec.getEdges().add(Pair.of(source.id(), sink.id()));

        Draft draft = DraftMaster.draft(spec);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec)
        );

        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        SparkForgeMaster forgeMaster = new SparkForgeMaster(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(forgeMaster);
        service.analyse(draft);
        service.exec();
    }

    private String json = "{\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"metals\" : [ {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.JsonFileMSource\",\n" +
            "    \"id\" : \"00-00\",\n" +
            "    \"name\" : \"source-00\",\n" +
            "    \"props\" : {\n" +
            "      \"schema\" : \"\",\n" +
            "      \"path\" : \"src/test/resources/test.json\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.ConsoleMSink\",\n" +
            "    \"id\" : \"01-00\",\n" +
            "    \"name\" : \"sink-00\",\n" +
            "    \"props\" : {\n" +
            "      \"numRows\" : 10\n" +
            "    }\n" +
            "  } ],\n" +
            "  \"edges\" : [ {\n" +
            "    \"left\" : \"00-00\",\n" +
            "    \"right\" : \"01-00\"\n" +
            "  } ]\n" +
            "}";

    @Test
    public void testConsoleSinkJson() throws IOException {
        Spec spec = new SpecFactoryOnJson().get(json);
        Draft draft = DraftMaster.draft(spec);

        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(
                new SparkForgeMaster(platform)
        );

        service.analyse(draft);
        System.out.println(service.analysed());
        System.out.println(service.unAnalysed());
        service.exec();
    }
}
