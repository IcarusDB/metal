package org.metal.backend.spark.extension.ml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.metal.backend.ISetup;
import org.metal.backend.spark.extension.ml.udf.AsVector;

public class AsVectorTest {
    @Test
    public void case0() throws JsonProcessingException {
        AsVector asVector = new AsVector();
        ObjectMapper mapper = new ObjectMapper();
        String val = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(asVector);
        System.out.println(val);
    }

    @Test
    public void case1() throws JsonProcessingException {
        String json = "{\n" +
                "  \"type\" : \"org.metal.backend.spark.extension.ml.udf.AsVector\"\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        AsVector asVector = (AsVector) mapper.readValue(json, ISetup.class);
        System.out.println(asVector.getName());
    }
}
