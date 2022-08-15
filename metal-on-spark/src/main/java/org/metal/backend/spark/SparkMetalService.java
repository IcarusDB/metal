package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.BaseMetalService;
import org.metal.core.translator.Translator;
import org.metal.core.props.IMetalProps;

public class SparkMetalService <P extends IMetalProps> extends BaseMetalService<Dataset<Row>, SparkSession, P> {
    protected SparkMetalService(Translator<Dataset<Row>, SparkSession> translator) {
        super(translator);
    }

    public static <P extends IMetalProps> SparkMetalService<P> of(SparkTranslator translator) {
        return new SparkMetalService<P>(translator);
    }
}
