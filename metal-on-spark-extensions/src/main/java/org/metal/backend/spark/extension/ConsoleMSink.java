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

package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSink;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.core.IMExecutor;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalTranslateException;

public class ConsoleMSink extends SparkMSink<IConsoleMSinkProps> {

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
