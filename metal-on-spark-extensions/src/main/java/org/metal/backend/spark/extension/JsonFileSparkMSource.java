package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSource;
import org.metal.core.exception.MetalForgeException;

public class JsonFileSparkMSource extends SparkMSource <IJsonFileSparkMSourceProps> {
    @JsonCreator
    public JsonFileSparkMSource(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IJsonFileSparkMSourceProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> source(SparkSession platform) throws MetalForgeException {
        try {
            return platform.read().json(this.props().path());
        } catch (Exception e) {
            throw new MetalForgeException(e);
        }
    }
}
