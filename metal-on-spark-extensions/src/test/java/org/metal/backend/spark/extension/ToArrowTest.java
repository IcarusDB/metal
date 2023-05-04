package org.metal.backend.spark.extension;

import java.util.Arrays;
import java.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

public class ToArrowTest {

  @Test
  public void case0() {
    SparkSession platform = SparkSession.builder()
        .appName("test")
        .master("local[*]")
        .getOrCreate();

    List<Row> dataTraining = Arrays.asList(
        RowFactory.create(1.0, "lee"),
        RowFactory.create(0.0, "le"),
        RowFactory.create(1.0, "lin")
    );
    StructType schema = new StructType(new StructField[]{
        new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
        new StructField("name", DataTypes.StringType, false, Metadata.empty())
    });

    Dataset<Row> training = platform.createDataFrame(dataTraining, schema);
//        training.toArrowBatchRdd().collect();
    training.show();
    training.toJSON().foreach((row) -> {
      System.out.println(row);
    });

  }
}
