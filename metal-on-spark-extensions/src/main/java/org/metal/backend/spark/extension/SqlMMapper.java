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

import org.metal.backend.spark.SparkMMapper;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.exception.MetalTranslateException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SqlMMapper extends SparkMMapper<ISqlMMapperProps> {

    @JsonCreator
    public SqlMMapper(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") ISqlMMapperProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> map(SparkSession platform, Dataset<Row> data)
            throws MetalTranslateException {
        SqlParserUtil.Tables tables = SqlParserUtil.table(this.props().sql());
        if (tables.primary().size() > 1) {
            String msg =
                    String.format(
                            "%s has access more than one tables %s.",
                            this.props().sql(), tables.primary());
            throw new MetalTranslateException(msg);
        }

        if (!SqlParserUtil.isQuery(this.props().sql())) {
            String msg =
                    String.format("%s must be one query like select clause.", this.props().sql());
            throw new MetalTranslateException(msg);
        }

        if (!tables.primary().contains(this.props().tableAlias())) {
            String msg =
                    String.format(
                            "%s never used configured table[%s].",
                            this.props().sql(), this.props().tableAlias());
            throw new MetalTranslateException(msg);
        }

        try {
            data.createOrReplaceTempView(this.props().tableAlias());
            Dataset<Row> ret = platform.sql(this.props().sql());
            return ret;
        } catch (Exception e) {
            throw new MetalTranslateException(e);
        }
    }

    @FormSchemaMethod
    public static String formSchema() {
        return FormJsonSchema.formSchema(ISqlMMapperProps.class);
    }
}
