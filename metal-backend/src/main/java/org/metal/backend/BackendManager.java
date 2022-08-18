package org.metal.backend;

import java.util.Optional;
import java.util.ServiceLoader;

public class BackendManager {
    public static Optional<IBackend.IBuilder> getBackendBuilder() {
        ServiceLoader<IBackend.IBuilder> loader = ServiceLoader.load(IBackend.IBuilder.class);
        return loader.findFirst();
    }

}
