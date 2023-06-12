/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.backend.spark.extension.ml.udf;

import org.metal.backend.ISetup;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;

import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.Serializable;

public class AsVector implements UDF1<Seq<Double>, Vector>, ISetup<SparkSession>, Serializable {

    @JsonProperty(value = "name")
    private String name = "as_vector";

    @JsonCreator
    public AsVector() {}

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
        for (double v : JavaConverters.asJavaCollection(field)) {
            fieldInJava[idx++] = v;
        }
        return new DenseVector(fieldInJava);
    }

    @Override
    public void setup(SparkSession platform) {
        platform.udf().register(this.name, this, new VectorUDT());
    }

    @FormSchemaMethod
    public static String formSchema() {
        return FormJsonSchema.formSchema(AsVector.class);
    }
}
