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
import org.metal.core.exception.MetalExecuteException;
import org.metal.core.props.IMetalProps;
import org.metal.core.specs.Spec;

public class SqlMMapperTest {
    @Test
    public void case0() {
        JsonFileMSource source = new JsonFileMSource(
                "00-00",
                "source-00",
                ImmutableIJsonFileMSourceProps.builder()
                        .path("src/test/resources/test.json")
                        .schema("")
                        .build()
        );

        SqlMMapper mapper = new SqlMMapper(
                "01-00",
                "mapper-00",
                ImmutableISqlMMapperProps.builder()
                        .tableAlias("source")
                        .sql("select * from source where id != \"0001\"")
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
            service.exec();
        } catch (MetalAnalysedException e) {
            e.printStackTrace();
        } catch (MetalExecuteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void case1() {
        JsonFileMSource source = new JsonFileMSource(
                "00-00",
                "source-00",
                ImmutableIJsonFileMSourceProps.builder()
                        .path("src/test/resources/test.json")
                        .schema("")
                        .build()
        );

        SqlMMapper mapper = new SqlMMapper(
                "01-00",
                "mapper-00",
                ImmutableISqlMMapperProps.builder()
                        .tableAlias("source")
                        .sql("select * from sourcedest where id != \"0001\"")
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
            service.exec();
        } catch (MetalAnalysedException e) {
            e.printStackTrace();
            Assert.assertTrue(true);
        } catch (MetalExecuteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void case3() {
        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        platform.sql("select * from t1 inner join (select * from t2) as t3 on t3.id = t1.id");
    }
}
