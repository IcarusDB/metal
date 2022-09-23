package org.metal.backend;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface IBackendRESTAPIProps {
  public String id();
  public Optional<Integer> port();
}
