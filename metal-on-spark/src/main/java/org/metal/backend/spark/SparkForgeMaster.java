package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;

public class SparkForgeMaster extends ForgeMaster<Dataset<Row>, SparkSession> {
    public SparkForgeMaster(SparkSession platform) {
        super(platform);
    }

    public SparkForgeMaster(SparkSession platform, ForgeContext<Dataset<Row>, SparkSession> context) {
        super(platform, context);
    }
}
