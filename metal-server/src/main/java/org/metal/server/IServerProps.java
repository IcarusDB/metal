package org.metal.server;

import org.immutables.value.Value;

@Value.Immutable
public interface IServerProps {
  public int port();
  public String mongoConnection();
}
