package org.metal.backend;

import java.util.Optional;
import java.util.ServiceLoader;

public class BackendManager {

  public static Optional<IBackend.IBuilder> getBackendBuilder() {
    ServiceLoader<IBackend.IBuilder> loader = ServiceLoader.load(IBackend.IBuilder.class);
    return loader.findFirst();
  }

  public static Optional<IBackend.IBuilder> getBackendBuilder(String clazz)
      throws IllegalArgumentException {
    if (clazz == null || clazz.strip().equals("")) {
      throw new IllegalArgumentException(String.format("Clazz{%s} is invalid.", clazz));
    }

    ServiceLoader<IBackend.IBuilder> loader = ServiceLoader.load(IBackend.IBuilder.class);
    return loader.stream().filter(
            (ServiceLoader.Provider<IBackend.IBuilder> provider) -> {
              return clazz.equals(provider.type().getName());
            }
        ).map(provider -> provider.get())
        .findFirst();
  }
}
