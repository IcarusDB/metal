package org.metal.backend;

import org.metal.service.BaseMetalService;

public interface IBackend {
    public void start() throws IllegalArgumentException;
    public void stop();
    public <R extends BaseMetalService> R service() throws IllegalArgumentException;
}
