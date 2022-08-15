package org.metal.core.backend;

import org.metal.core.BaseMetalService;

public interface IBackend {
    public void start() throws IllegalArgumentException;
    public void stop();
    public <R extends BaseMetalService> R service() throws IllegalArgumentException;
}
