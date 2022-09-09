package org.metal.backend.api.impl;

import org.immutables.value.Value;

@Value.Immutable
public interface IBackendServiceProps {
  public String reportAddress();
  public String backendServiceAddress();
}
