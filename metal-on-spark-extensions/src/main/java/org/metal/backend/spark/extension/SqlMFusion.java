package org.metal.backend.spark.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMFusion;
import org.metal.core.exception.MetalForgeException;

import java.util.Map;

public class SqlMFusion extends SparkMFusion<ISqlMFusionProps> {
    @JsonCreator
    public SqlMFusion(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") ISqlMFusionProps props) {
        super(id, name, props);
    }

    @Override
    public Dataset<Row> fusion(SparkSession platform, Map<String, Dataset<Row>> datas) throws MetalForgeException {
        if (!this.props().tableAlias().keySet().equals(datas.keySet())) {
            String msg = String.format("The metal[%s]\'s dependency is %s, but TableAlias is %s. These should be same.",
                    this.id(),
                    datas.keySet(),
                    this.props().tableAlias().keySet()
            );
            throw new MetalForgeException(msg);
        }

        SqlParserUtil.Tables tables = SqlParserUtil.table(this.props().sql());
        if (tables.primary().size() <= 1) {
            String msg = String.format("%s should access more than one primary table.", this.props().sql());
            throw new MetalForgeException(msg);
        }

        if (!SqlParserUtil.isQuery(this.props().sql())) {
            String msg = String.format("%s must be one query like select clause.", this.props().sql());
            throw new MetalForgeException(msg);
        }

        if (!tables.primary().equals(this.props().tableAlias().values())) {
            String msg = String.format("%s accessed table should be same with configured Table Alias{%s}.", this.props().sql(), this.props().tableAlias().values());
            throw new MetalForgeException(msg);
        }

        for(Map.Entry<String, Dataset<Row>> data: datas.entrySet()) {
            String id = data.getKey();
            Dataset<Row> dataset = data.getValue();
            String tempTableName = this.props().tableAlias().get(id);
            dataset.createOrReplaceTempView(tempTableName);
        }
        try {
            return platform.sql(this.props().sql());
        } catch (Exception e) {
            throw new MetalForgeException(e);
        }
    }
}