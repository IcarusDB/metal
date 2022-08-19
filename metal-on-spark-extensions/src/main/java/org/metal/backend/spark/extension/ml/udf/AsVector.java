package org.metal.backend.spark.extension.ml.udf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.metal.backend.ISetup;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.Serializable;

public class AsVector implements UDF1<Seq<Double>, Vector>, ISetup<SparkSession>, Serializable {
    @JsonProperty(value = "name")
    private String name = "as_vector";

    @JsonCreator
    public AsVector() {
    }

    public AsVector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Vector call(Seq<Double> field) throws Exception {
        double[] fieldInJava = new double[field.size()];
        int idx = 0;
        for (double v : JavaConverters.asJava(field)) {
            fieldInJava[idx++] = v;
        }
        return new DenseVector(fieldInJava);
    }

    @Override
    public void setup(SparkSession platform) {
        platform.udf().register(this.name, this, new VectorUDT());
    }
}
