package org.metal.dataset.spark;

import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.spark.sql.types.*;
import org.apache.spark.sql.util.ArrowUtils;
import org.metal.dataset.SchemaConvertor;


public class SchemaConvertorWithSpark implements SchemaConvertor<StructType> {
    @Override
    public StructType to(Schema schema) {
        return ArrowUtils.fromArrowSchema(schema);
    }

    @Override
    public Schema from(StructType schema, String timeZoneId) {
        return ArrowUtils.toArrowSchema(schema, timeZoneId);
    }
}
