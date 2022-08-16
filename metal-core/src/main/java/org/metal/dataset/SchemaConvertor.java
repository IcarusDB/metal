package org.metal.dataset;

import org.apache.arrow.vector.types.pojo.Schema;

public interface SchemaConvertor<T>{
    public T to(Schema schema);
    public Schema from(T schema, String timeZoneId);
}
