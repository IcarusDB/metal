package org.metal.backend;

import java.util.Optional;
import java.util.ServiceLoader;

public class BackendDeployManager {
    public static Optional<IBackendDeploy> getBackendDeploy() {
        ServiceLoader<IBackendDeploy> loader = ServiceLoader.load(IBackendDeploy.class);
        return loader.findFirst();
    }

    public static Optional<IBackendDeploy> getBackendDeploy(String clazz) throws IllegalArgumentException{
        if (clazz == null || clazz.strip().equals("")) {
            throw new IllegalArgumentException(String.format("Clazz{%s} is invalid.", clazz));
        }

        ServiceLoader<IBackendDeploy> loader = ServiceLoader.load(IBackendDeploy.class);
        return loader.stream().filter(
                        (ServiceLoader.Provider<IBackendDeploy> provider) -> {
                            return clazz.equals(provider.type().getName());
                        }
                ).map(provider -> provider.get())
                .findFirst();
    }
}
