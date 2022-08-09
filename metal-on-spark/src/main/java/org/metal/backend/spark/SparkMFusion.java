package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.MFusion;
import org.metal.core.props.IMFusionProps;

public abstract class SparkMFusion <P extends IMFusionProps> extends MFusion <Dataset<Row>, SparkSession, P> {
    public SparkMFusion(String id, String name, P props) {
        super(id, name, props);
    }
}
