package org.metal.backend.spark.extension.ml.udf;

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
        platform.udf().register("as_vector", this, new VectorUDT());
    }
}
