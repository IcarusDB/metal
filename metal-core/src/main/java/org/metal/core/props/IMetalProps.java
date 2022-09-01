package org.metal.core.props;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.io.Serializable;

//@JsonTypeInfo(use = Id.CLASS, property = "type", include = As.PROPERTY, visible = false)
public interface IMetalProps extends Serializable {
}
