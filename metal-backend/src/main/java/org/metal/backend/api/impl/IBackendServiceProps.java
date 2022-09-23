package org.metal.backend.api.impl;

import org.immutables.value.Value;

@Value.Immutable
public interface IBackendServiceProps {
  public String reportServiceAddress();
  public String backendServiceAddress();
}
