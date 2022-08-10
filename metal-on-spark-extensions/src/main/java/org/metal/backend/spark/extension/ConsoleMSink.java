package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSink;
import org.metal.core.exception.MetalExecuteException;

public class ConsoleMSink extends SparkMSink <IConsoleMSinkProps> {

    @JsonCreator
    public ConsoleMSink(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IConsoleMSinkProps props) {
        super(id, name, props);
    }

    @Override
    public void sink(SparkSession platform, Dataset<Row> data) throws MetalExecuteException {
        try {
            data.show(this.props().numRows());
        } catch (Exception e) {
            throw new MetalExecuteException(e);
        }
    }
}
