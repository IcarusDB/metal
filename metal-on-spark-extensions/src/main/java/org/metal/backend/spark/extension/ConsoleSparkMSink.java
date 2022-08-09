package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSink;

public class ConsoleSparkMSink extends SparkMSink <IConsoleSparkMSinkProps> {

    @JsonCreator
    public ConsoleSparkMSink(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IConsoleSparkMSinkProps props) {
        super(id, name, props);
    }

    @Override
    public void sink(SparkSession platform, Dataset<Row> data) {
        data.show(this.props().numRows());
    }
}
