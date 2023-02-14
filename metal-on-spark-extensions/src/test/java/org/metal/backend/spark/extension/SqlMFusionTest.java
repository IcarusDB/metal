package org.metal.backend.spark.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.Test;
import org.metal.backend.spark.SparkMetalService;
import org.metal.backend.spark.SparkTranslator;
import org.metal.core.FormSchemaMethods;
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
                        .sql("select * from tbl1 left join tbl0 on tbl0.id = tbl1.id")
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
}
