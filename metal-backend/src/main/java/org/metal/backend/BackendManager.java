/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return loader.stream()
                .filter(
                        (ServiceLoader.Provider<IBackend.IBuilder> provider) -> {
                            return clazz.equals(provider.type().getName());
                        })
                .map(provider -> provider.get())
                .findFirst();
    }
}
