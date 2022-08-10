package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMMapper;
import org.metal.core.exception.MetalForgeException;

public class WhereSparkMMapper extends SparkMMapper <IWhereSparkMMapperProps> {
    @JsonCreator
    public WhereSparkMMapper(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") IWhereSparkMMapperProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> map(SparkSession platform, Dataset<Row> data) throws MetalForgeException {
        try {
            return data.where(this.props().conditionExpr());
        } catch (Exception e) {
            throw new MetalForgeException(e);
        }
    }
}
