package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSink;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.core.IMExecutor;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalTranslateException;

public class ConsoleMSink extends SparkMSink <IConsoleMSinkProps> {

    @JsonCreator
    public ConsoleMSink(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IConsoleMSinkProps props) {
        super(id, name, props);
    }

    @Override
    public IMExecutor sink(SparkSession platform, Dataset<Row> data) throws MetalTranslateException {
        return () -> {
            try {
                data.show(this.props().numRows());
            } catch (Exception e) {
                throw new MetalExecuteException(e);
            }
        };
    }

    @FormSchemaMethod
    public static String formSchema() {
        return FormJsonSchema.formSchema(IConsoleMSinkProps.class);
    }
}
