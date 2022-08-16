package org.metal.backend.spark.extension.ml;

import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LogisticRegressionLearnerTest {
    @Test
    public void case0() throws IOException {
        SparkSession platform = SparkSession.builder()
                .appName("test")
                .master("local[*]")
                .getOrCreate();

        List<Row> dataTraining = Arrays.asList(
                RowFactory.create(1.0, Vectors.dense(0.0, 1.1, 0.1)),
                RowFactory.create(0.0, Vectors.dense(2.0, 1.0, -1.0)),
                RowFactory.create(0.0, Vectors.dense(2.0, 1.3, 1.0)),
                RowFactory.create(1.0, Vectors.dense(0.0, 1.2, -0.5))
        );
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });

        Dataset<Row> training = platform.createDataFrame(dataTraining, schema);
        LogisticRegressionLearner learner = new LogisticRegressionLearner(
                "00-00",
                "l-00",
                ImmutableILogisticRegressionLearnerProps.builder()
                        .labelCol("label")
                        .featuresCol("features")
                        .initialModel("./src/test/resources/learner.model")
                        .savePath("./src/test/resources/learner-0.model")
                        .build()
        );

        learner.sink(platform, training).exec();
    }
}
