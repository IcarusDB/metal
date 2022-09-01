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
import org.metal.backend.spark.SparkMSource;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.exception.MetalTranslateException;

public class JsonFileMSource extends SparkMSource <IJsonFileMSourceProps> {
    @JsonCreator
    public JsonFileMSource(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IJsonFileMSourceProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> source(SparkSession platform) throws MetalTranslateException {
        try {
            return platform.read().json(this.props().path());
        } catch (Exception e) {
            throw new MetalTranslateException(e);
        }
    }

    @FormSchemaMethod
    public static String formSchema() {
        return FormJsonSchema.formSchema(IJsonFileMSourceProps.class);
    }
}
