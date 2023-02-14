package org.metal.backend.spark.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.metal.backend.spark.SparkMetalService;
import org.metal.backend.spark.SparkTranslator;
import org.metal.core.Pair;
import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

public class SqlMFusionTest {
    @Test
    public void case0() {
        JsonFileMSource source0 = new JsonFileMSource(
                "00-00",
                "source-00",
                ImmutableIJsonFileMSourceProps.builder()
                        .path("src/test/resources/test.json")
                        .schema("")
                        .build()
        );

        JsonFileMSource source1 = new JsonFileMSource(
            "00-01",
            "source-01",
            ImmutableIJsonFileMSourceProps.builder()
                .path("src/test/resources/test.json")
                .schema("")
                .build()
        );

        Map<String, String> alias = new HashMap<>();
        alias.put("00-00", "tbl0");
        alias.put("00-01", "tbl1");
        SqlMFusion fusion = new SqlMFusion(
                "01-00",
                "fusion-00",
                ImmutableISqlMFusionProps.builder()
                        .tableAlias(alias)
                        .sql("select * from tbl1 left join tbl0 on tbl0.id = tbl1.")
                        .build()
        );

        ConsoleMSink sink = new ConsoleMSink(
                "02-00",
                "sink-00",
                ImmutableIConsoleMSinkProps.builder()
                        .numRows(10)
                        .build()
        );

        Spec spec = new Spec("1.0");
        spec.getMetals().add(source0);
        spec.getMetals().add(source1);
        spec.getMetals().add(fusion);
        spec.getMetals().add(sink);

        spec.getEdges().add(Pair.of("00-00", "01-00"));
        spec.getEdges().add(Pair.of("00-01", "01-00"));
        spec.getEdges().add(Pair.of("01-00", "02-00"));
        try {
            System.out.println(
                new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(spec)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Draft draft = DraftMaster.draft(spec);

        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        SparkTranslator forgeMaster = new SparkTranslator(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(forgeMaster);
        try {
            service.analyse(draft);
            service.exec();
        } catch (MetalAnalysedException e) {
            e.printStackTrace();
        } catch (MetalExecuteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void case1() {
        Spec spec = new SpecFactoryOnJson().get(payload);
        spec.getMetals().forEach(m -> {
            System.out.println(m);
        });
    }


    private String payload = "{\n"
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
        + "    \"type\" : \"org.metal.backend.spark.extension.JsonFileMSource\",\n"
        + "    \"id\" : \"00-01\",\n"
        + "    \"name\" : \"source-01\",\n"
        + "    \"props\" : {\n"
        + "      \"schema\" : \"\",\n"
        + "      \"path\" : \"src/test/resources/test.json\"\n"
        + "    }\n"
        + "  }, {\n"
        + "    \"type\" : \"org.metal.backend.spark.extension.SqlMFusion\",\n"
        + "    \"id\" : \"01-00\",\n"
        + "    \"name\" : \"fusion-00\",\n"
        + "    \"props\" : {\n"
        + "      \"tableAlias\" : {\n"
        + "        \"00-01\" : \"tbl1\",\n"
        + "        \"00-00\" : \"tbl0\"\n"
        + "      },\n"
        + "      \"sql\" : \"select * from tbl1 left join tbl0 on tbl0.id = tbl1.id\"\n"
        + "    }\n"
        + "  }, {\n"
        + "    \"type\" : \"org.metal.backend.spark.extension.ConsoleMSink\",\n"
        + "    \"id\" : \"02-00\",\n"
        + "    \"name\" : \"sink-00\",\n"
        + "    \"props\" : {\n"
        + "      \"numRows\" : 10\n"
        + "    }\n"
        + "  } ],\n"
        + "  \"edges\" : [ {\n"
        + "    \"left\" : \"00-00\",\n"
        + "    \"right\" : \"01-00\"\n"
        + "  }, {\n"
        + "    \"left\" : \"00-01\",\n"
        + "    \"right\" : \"01-00\"\n"
        + "  }, {\n"
        + "    \"left\" : \"01-00\",\n"
        + "    \"right\" : \"02-00\"\n"
        + "  } ],\n"
        + "  \"waitFor\" : [ ]\n"
        + "}";
    
    private static String payload1 = "{\"version\":\"1.0\",\"metals\":[{\"type\":\"org.metal.backend.spark.extension.SqlMFusion\",\"id\":\"node_2\",\"name\":\"node_2\",\"props\":{\"tableAlias\":{\"node_1\":\"tbl1\",\"node_0\":\"tbl0\"},\"sql\":\"select * from tbl0 left join tbl1 on tbl0.id = tbl1.id\"}},{\"type\":\"org.metal.backend.spark.extension.JsonFileMSource\",\"id\":\"node_1\",\"name\":\"node_1\",\"props\":{\"schema\":\"json\",\"path\":\"hdfs://namenode.hdfs.metal.org:9000/metal/test.json\"}},{\"type\":\"org.metal.backend.spark.extension.JsonFileMSource\",\"id\":\"node_0\",\"name\":\"node_0\",\"props\":{\"path\":\"hdfs://namenode.hdfs.metal.org:9000/metal/test.json\",\"schema\":\"json\"}}],\"edges\":[{\"left\":\"node_0\",\"right\":\"node_2\"},{\"left\":\"node_1\",\"right\":\"node_2\"}],\"waitFor\":[]}";
}
