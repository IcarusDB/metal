package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.immutables.value.Value;
import org.metal.core.props.IMFusionProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableISqlMFusionProps.class)
@JsonSerialize(as = ImmutableISqlMFusionProps.class)
public interface ISqlMFusionProps extends IMFusionProps {
    @JsonDeserialize(using = TableAliasDeSer.class)
    public Map<String, String> tableAlias();
    public String sql();
}
