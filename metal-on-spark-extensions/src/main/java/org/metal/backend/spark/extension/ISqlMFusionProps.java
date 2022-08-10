package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMFusionProps;

import java.util.Map;

@Value.Immutable
@JsonDeserialize(as = ImmutableISqlMFusionProps.class)
@JsonSerialize(as = ImmutableISqlMFusionProps.class)
public interface ISqlMFusionProps extends IMFusionProps {
    public Map<String, String> tableAlias();
    public String sql();
}
