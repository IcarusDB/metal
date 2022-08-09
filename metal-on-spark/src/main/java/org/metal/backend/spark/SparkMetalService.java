package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.BaseMetalService;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMetalProps;

public class SparkMetalService <P extends IMetalProps> extends BaseMetalService<Dataset<Row>, SparkSession, P> {
    protected SparkMetalService(ForgeMaster<Dataset<Row>, SparkSession> forgeMaster) {
        super(forgeMaster);
    }

    public static <P extends IMetalProps> SparkMetalService<P> of(SparkForgeMaster forgeMaster) {
        return new SparkMetalService<P>(forgeMaster);
    }
}
