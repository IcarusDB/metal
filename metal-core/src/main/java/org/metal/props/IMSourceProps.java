package org.metal.props;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.props.IMetalProps;

public interface IMSourceProps extends IMetalProps {
    public String schema();
}
