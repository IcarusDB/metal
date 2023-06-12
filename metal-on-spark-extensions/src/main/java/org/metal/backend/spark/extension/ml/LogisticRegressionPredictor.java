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

package org.metal.backend.spark.extension.ml;

import org.metal.backend.spark.SparkMMapper;
import org.metal.core.FormSchemaMethod;
import org.metal.exception.MetalTranslateException;

import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LogisticRegressionPredictor extends SparkMMapper<ILogisticRegressionPredictorProps> {

    @JsonCreator
    public LogisticRegressionPredictor(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") ILogisticRegressionPredictorProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> map(SparkSession platform, Dataset<Row> data)
            throws MetalTranslateException {
        ILogisticRegressionPredictorProps props = this.props();
        LogisticRegressionModel model = LogisticRegressionModel.load(props.modelPath());

        props.thresholds().ifPresent(v -> model.setThresholds(Convertor.convert2double(v)));
        props.threshold().ifPresent(v -> model.setThreshold(v));
        props.predictionCol().ifPresent(v -> model.setPredictionCol(v));
        props.probabilityCol().ifPresent(v -> model.setProbabilityCol(v));
        props.rawPredictionCol().ifPresent(v -> model.setRawPredictionCol(v));

        model.setFeaturesCol(props.featuresCol());

        return model.transform(data);
    }

    @FormSchemaMethod
    public static String formSchema() {
        String schema =
                "{\n"
                        + "     \"uiSchema\" : {\n"
                        + "      \"ui:order\": [\n"
                        + "        \"modelPath\",\n"
                        + "        \"featuresCol\",\n"
                        + "        \"*\"\n"
                        + "      ]\n"
                        + "     },"
                        + "    \"formSchema\" : {\n"
                        + "      \"type\" : \"object\",\n"
                        + "      \"id\" : \"urn:jsonschema:org:metal:backend:spark:extension:ml:ILogisticRegressionPredictorProps\",\n"
                        + "      \"required\": [\n"
                        + "        \"modelPath\",\n"
                        + "        \"featuresCol\"\n"
                        + "      ],\n"
                        + "      \"properties\" : {\n"
                        + "        \"featuresCol\" : {\n"
                        + "          \"type\" : \"string\"\n"
                        + "        },\n"
                        + "        \"predictionCol\" : {\n"
                        + "          \"type\" : \"string\"\n"
                        + "        },\n"
                        + "        \"probabilityCol\" : {\n"
                        + "          \"type\" : \"string\"\n"
                        + "        },\n"
                        + "        \"rawPredictionCol\" : {\n"
                        + "          \"type\" : \"string\"\n"
                        + "        },\n"
                        + "        \"thresholds\" : {\n"
                        + "          \"type\" : \"array\",\n"
                        + "          \"items\" : {\n"
                        + "            \"type\" : \"number\"\n"
                        + "          }\n"
                        + "        },\n"
                        + "        \"threshold\" : {\n"
                        + "          \"type\" : \"number\"\n"
                        + "        },\n"
                        + "        \"modelPath\" : {\n"
                        + "          \"type\" : \"string\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  } ";
        return schema;
    }
}
