package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMMapperProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableISqlMMapperProps.class)
@JsonSerialize(as = ImmutableISqlMMapperProps.class)
public interface ISqlMMapperProps extends IMMapperProps {

  public String tableAlias();

  public String sql();
}
