package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMMapper;
import org.metal.core.FormJsonSchema;
import org.metal.core.FormSchemaMethod;
import org.metal.exception.MetalTranslateException;

public class WhereMMapper extends SparkMMapper<IWhereMMapperProps> {

  @JsonCreator
  public WhereMMapper(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("props") IWhereMMapperProps props) {
    super(id, name, props);
  }

  @Override
  public Dataset<Row> map(SparkSession platform, Dataset<Row> data) throws MetalTranslateException {
    try {
      return data.where(this.props().conditionExpr());
    } catch (Exception e) {
      throw new MetalTranslateException(e);
    }
  }

  @FormSchemaMethod
  public static String formSchema() {
    return FormJsonSchema.formSchema(IWhereMMapperProps.class);
  }
}
