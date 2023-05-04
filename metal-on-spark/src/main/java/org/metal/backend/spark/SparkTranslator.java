package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

public class SparkTranslator extends Translator<Dataset<Row>, SparkSession> {

  public SparkTranslator(SparkSession platform) {
    super(platform);
  }

  public SparkTranslator(SparkSession platform,
      TranslatorContext<Dataset<Row>, SparkSession> context) {
    super(platform, context);
  }
}
