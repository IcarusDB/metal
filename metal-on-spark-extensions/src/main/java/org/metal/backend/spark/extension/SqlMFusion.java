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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMFusion;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.exception.MetalTranslateException;

public class SqlMFusion extends SparkMFusion<ISqlMFusionProps> {

  @JsonCreator
  public SqlMFusion(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("props") ISqlMFusionProps props) {
    super(id, name, props);
  }

  @Override
  public Dataset<Row> fusion(SparkSession platform, Map<String, Dataset<Row>> datas)
      throws MetalTranslateException {
    if (!this.props().tableAlias().keySet().equals(datas.keySet())) {
      String msg = String.format(
          "The metal[%s]\'s dependency is %s, but TableAlias is %s. These should be same.",
          this.id(),
          datas.keySet(),
          this.props().tableAlias().keySet()
      );
      throw new MetalTranslateException(msg);
    }

    SqlParserUtil.Tables tables = SqlParserUtil.table(this.props().sql());
    if (tables.primary().size() <= 1) {
      String msg = String.format("%s should access more than one primary table.",
          this.props().sql());
      throw new MetalTranslateException(msg);
    }

    if (!SqlParserUtil.isQuery(this.props().sql())) {
      String msg = String.format("%s must be one query like select clause.", this.props().sql());
      throw new MetalTranslateException(msg);
    }

    if (isTablesOutRange(tables.primary(),
        new HashSet<String>(this.props().tableAlias().values()))) {
      String msg = String.format(
          "%s accessed table should be same with configured Table Alias{%s}.", this.props().sql(),
          this.props().tableAlias().values());
      throw new MetalTranslateException(msg);
    }

    for (Map.Entry<String, Dataset<Row>> data : datas.entrySet()) {
      String id = data.getKey();
      Dataset<Row> dataset = data.getValue();
      String tempTableName = this.props().tableAlias().get(id);
      dataset.createOrReplaceTempView(tempTableName);
    }
    try {
      return platform.sql(this.props().sql());
    } catch (Exception e) {
      throw new MetalTranslateException(e);
    }
  }

  private static boolean isTablesOutRange(Set<String> tables, Set<String> alias) {
    for (String table : tables) {
      if (!alias.contains(table)) {
        return true;
      }
    }
    return false;
  }

  @FormSchemaMethod
  public static String formSchema() {
    return FormJsonSchema.formSchema(ISqlMFusionProps.class);
  }
}
