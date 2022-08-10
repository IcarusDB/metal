package org.metal.backend.spark.extension;

import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.Test;
import org.metal.backend.spark.SparkForgeMaster;
import org.metal.backend.spark.SparkMetalService;
import org.metal.core.Pair;
import org.metal.core.draft.Draft;
import org.metal.core.draft.DraftMaster;
import org.metal.core.exception.MetalAnalysedException;
import org.metal.core.props.IMetalProps;
import org.metal.core.specs.Spec;

public class WhereSparkMMapperTest {
    @Test
    public void case0() {
        JsonFileSparkMSource source = new JsonFileSparkMSource(
                "00-00",
                "source-00",
                ImmutableIJsonFileSparkMSourceProps.builder()
                        .path("src/test/resources/test.json")
                        .schema("")
                        .build()
        );

        WhereSparkMMapper mapper = new WhereSparkMMapper(
                "01-00",
                "mapper-00",
                ImmutableIWhereSparkMMapperProps.builder()
                        .conditionExpr("ids = \"0001\"")
                        .build()
        );

        ConsoleSparkMSink sink = new ConsoleSparkMSink(
                "02-00",
                "sink-00",
                ImmutableIConsoleSparkMSinkProps.builder()
                        .numRows(10)
                        .build()
        );

        Spec spec = new Spec("1.0");
        spec.getMetals().add(source);
        spec.getMetals().add(mapper);
        spec.getMetals().add(sink);

        spec.getEdges().add(Pair.of("00-00", "01-00"));
        spec.getEdges().add(Pair.of("01-00", "02-00"));

        Draft draft = DraftMaster.draft(spec);

        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        SparkForgeMaster forgeMaster = new SparkForgeMaster(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(forgeMaster);
        try {
            service.analyse(draft);
        } catch (MetalAnalysedException e) {
            System.out.println("==============unAnalysed");
            System.out.println(service.unAnalysed());
            System.out.println("================Analysed");
            System.out.println(service.analysed());
            Assert.assertTrue(true);
        }
    }
}
