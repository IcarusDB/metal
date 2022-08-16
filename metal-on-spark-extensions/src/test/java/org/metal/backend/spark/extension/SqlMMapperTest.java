package org.metal.backend.spark.extension;

import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.Test;
import org.metal.backend.spark.SparkTranslator;
import org.metal.backend.spark.SparkMetalService;
import org.metal.core.Pair;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.core.props.IMetalProps;
import org.metal.specs.Spec;

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

        SparkTranslator forgeMaster = new SparkTranslator(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(forgeMaster);
        try {
            service.analyse(draft);
            service.exec();
        } catch (MetalAnalysedException e) {
            Assert.assertTrue(true);
        } catch (MetalExecuteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void case2() {
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

        ConsoleMSink sink1 = new ConsoleMSink(
                "02-01",
                "sink-01",
                ImmutableIConsoleMSinkProps.builder()
                        .numRows(10)
                        .build()
        );

        Spec spec = new Spec("1.0");
        spec.getMetals().add(source);
        spec.getMetals().add(mapper);
        spec.getMetals().add(sink);
        spec.getMetals().add(sink1);

        spec.getEdges().add(Pair.of("00-00", "01-00"));
        spec.getEdges().add(Pair.of("01-00", "02-00"));
        spec.getEdges().add(Pair.of("00-00", "02-01"));

        spec.getWaitFor().add(Pair.of("02-00", "02-01"));

        Draft draft = DraftMaster.draft(spec);

        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        SparkTranslator sparkTranslator = new SparkTranslator(platform);
        SparkMetalService<IMetalProps> service = SparkMetalService.<IMetalProps>of(sparkTranslator);
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
