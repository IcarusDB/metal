package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMMapper;
import org.metal.core.exception.MetalForgeException;

public class SqlMMapper extends SparkMMapper <ISqlMMapperProps> {
    @JsonCreator
    public SqlMMapper(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") ISqlMMapperProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> map(SparkSession platform, Dataset<Row> data) throws MetalForgeException {
        SqlParserUtil.Tables tables = SqlParserUtil.table(this.props().sql());
        if (tables.primary().size() > 1) {
            String msg = String.format("%s has access more than one tables %s.", this.props().sql(), tables.primary());
            throw new MetalForgeException(msg);
        }

        if (!SqlParserUtil.isQuery(this.props().sql())) {
            String msg = String.format("%s must be one query like select clause.", this.props().sql());
            throw new MetalForgeException(msg);
        }

        if (!tables.primary().contains(this.props().tableAlias())) {
            String msg = String.format("%s never used configured table[%s].", this.props().sql(), this.props().tableAlias());
            throw new MetalForgeException(msg);
        }

        try {
            data.createOrReplaceTempView(this.props().tableAlias());
            Dataset<Row> ret = platform.sql(this.props().sql());
            return ret;
        } catch (Exception e) {
            throw new MetalForgeException(e);
        }
    }
}
