package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.MSink;
import org.metal.core.props.IMSinkProps;

public abstract class SparkMSink <P extends IMSinkProps> extends MSink<Dataset<Row>, SparkSession, P> {
    public SparkMSink(String id, String name, P props) {
        super(id, name, props);
    }
}
