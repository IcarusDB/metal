package org.metal.backend;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type", include = JsonTypeInfo.As.PROPERTY, visible = false)
public interface ISetup<S> {
    public void setup(S platform);
}
