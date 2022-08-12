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
}
