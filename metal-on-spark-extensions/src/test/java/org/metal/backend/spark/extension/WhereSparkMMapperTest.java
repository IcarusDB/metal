package org.metal.backend.spark.extension;

import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.Test;
import org.metal.backend.spark.SparkMetalService;
import org.metal.backend.spark.SparkTranslator;
import org.metal.core.Pair;
import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.specs.Spec;

public class WhereSparkMMapperTest {

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

    WhereMMapper mapper = new WhereMMapper(
        "01-00",
        "mapper-00",
        ImmutableIWhereMMapperProps.builder()
            .conditionExpr("ids = \"0001\"")
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
    } catch (MetalAnalysedException e) {
      System.out.println("==============unAnalysed");
      System.out.println(service.unAnalysed());
      System.out.println("================Analysed");
      System.out.println(service.analysed());
      Assert.assertTrue(true);
    }
  }
}
