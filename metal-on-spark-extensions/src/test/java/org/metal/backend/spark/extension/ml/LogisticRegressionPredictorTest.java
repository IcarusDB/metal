package org.metal.backend.spark.extension.ml;

import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import org.metal.backend.spark.SparkBackend;
import org.metal.backend.spark.SparkMetalService;
import org.metal.backend.spark.extension.*;
import org.metal.backend.spark.extension.ml.udf.AsVector;
import org.metal.core.draft.Draft;
import org.metal.core.props.IMetalProps;

import java.util.Arrays;
import java.util.List;

public class LogisticRegressionPredictorTest {
    @Test
    public void case0() {
        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        List<Row> dataTest = Arrays.asList(
                RowFactory.create(1.0, Vectors.dense(-1.0, 1.5, 1.3)),
                RowFactory.create(0.0, Vectors.dense(3.0, 2.0, -0.1)),
                RowFactory.create(1.0, Vectors.dense(0.0, 2.2, -1.5))
        );
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        Dataset<Row> test = platform.createDataFrame(dataTest, schema);

        LogisticRegressionPredictor predictor = new LogisticRegressionPredictor(
                "1-00",
                "p-00",
                ImmutableILogisticRegressionPredictorProps.builder()
                        .featuresCol("features")
                        .modelPath("./src/test/resources/learner.model")
                        .build()
        );

        predictor.map(platform, test).show();

    }

    @Test
    public void case1() {
        SparkBackend backend = SparkBackend.builder()
                .conf("master", "local[*]")
                .setup(new AsVector())
                .build();
        backend.start();

        SparkMetalService<IMetalProps> service = backend.service();

        JsonFileMSource source = new JsonFileMSource(
                "00-00",
                "source-00",
                ImmutableIJsonFileMSourceProps.builder()
                        .schema("")
                        .path("src/test/resources/data.json")
                        .build()
        );

        SqlMMapper mapper = new SqlMMapper(
                "01-00",
                "mapper-00",
                ImmutableISqlMMapperProps.builder()
                        .tableAlias("data")
                        .sql("select label, as_vector(features) as vfeatures from data")
                        .build()
        );

        LogisticRegressionLearner learner = new LogisticRegressionLearner(
                "02-00",
                "sink-00",
                ImmutableILogisticRegressionLearnerProps.builder()
                        .featuresCol("vfeatures")
                        .labelCol("label")
                        .maxIter(10)
                        .savePath("src/test/resources/lr-model")
                        .build()
        );

        LogisticRegressionPredictor predictor = new LogisticRegressionPredictor(
                "01-01",
                "mapper-01",
                ImmutableILogisticRegressionPredictorProps.builder()
                        .featuresCol("vfeatures")
                        .predictionCol("pred-label")
                        .probabilityCol("prod")
                        .modelPath("src/test/resources/lr-model")
                        .build()
        );

        ConsoleMSink console = new ConsoleMSink(
                "02-01",
                "sink-01",
                ImmutableIConsoleMSinkProps.builder()
                        .numRows(10)
                        .build()
        );

        Draft draft = Draft.builder()
                .add(source)
                .add(mapper)
                .add(learner)
                .add(predictor)
                .add(console)
                .addEdge(source, mapper)
                .addEdge(mapper, learner)
                .addEdge(mapper, predictor)
                .addEdge(predictor, console)
                .withWait()
                .waitFor(predictor, learner)
                .build();

        service.analyse(draft);
        System.out.println(service.analysed());
        service.exec();

    }
}
