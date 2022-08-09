package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.MMapper;
import org.metal.core.props.IMMapperProps;

public abstract class SparkMMapper <P extends IMMapperProps> extends MMapper<Dataset<Row>, SparkSession, P> {
    public SparkMMapper(String id, String name, P props) {
        super(id, name, props);
    }
}
