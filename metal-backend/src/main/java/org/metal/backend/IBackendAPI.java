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

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public interface IBackendAPI {

    public void analyseAPI(RoutingContext ctx);

    public void schemaAPI(RoutingContext ctx);

    public void heartAPI(RoutingContext ctx);

    public void statusAPI(RoutingContext ctx);

    public void execAPI(RoutingContext ctx);

    public static interface IBackendTryAPI {

        public void tryAnalyseAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire);

        public void trySchemaAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire);

        public void tryStatusAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire);

        public void tryExecAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire);
    }

    public static ConcurrentBackendAPI of(IBackendAPI API) {
        return new ConcurrentBackendAPI(API);
    }

    public static class ConcurrentBackendAPI implements IBackendAPI, IBackendTryAPI {

        private IBackendAPI API;
        private ReadLock analyseReadLock;
        private WriteLock analyseWriteLock;
        private ReentrantLock execLock;

        private ConcurrentBackendAPI(IBackendAPI API) throws IllegalArgumentException {
            if (API instanceof ConcurrentBackendAPI) {
                String msg =
                        String.format(
                                "Fail to construct object, because the parameter API is one instance of %s",
                                ConcurrentBackendAPI.class);
                throw new IllegalArgumentException(msg);
            }
            this.API = API;
            ReentrantReadWriteLock analyseLock = new ReentrantReadWriteLock();
            this.analyseReadLock = analyseLock.readLock();
            this.analyseWriteLock = analyseLock.writeLock();
            this.execLock = new ReentrantLock();
        }

        @Override
        public void analyseAPI(RoutingContext ctx) {
            analyseWriteLock.lock();
            try {
                API.analyseAPI(ctx);
            } finally {
                analyseWriteLock.unlock();
            }
        }

        @Override
        public void schemaAPI(RoutingContext ctx) {
            analyseReadLock.lock();
            try {
                API.schemaAPI(ctx);
            } finally {
                analyseReadLock.unlock();
            }
        }

        @Override
        public void heartAPI(RoutingContext ctx) {
            API.heartAPI(ctx);
        }

        @Override
        public void statusAPI(RoutingContext ctx) {
            analyseReadLock.lock();
            try {
                API.statusAPI(ctx);
            } finally {
                analyseReadLock.unlock();
            }
        }

        @Override
        public void execAPI(RoutingContext ctx) {
            analyseReadLock.lock();
            try {
                execLock.lock();
                try {
                    API.execAPI(ctx);
                } finally {
                    execLock.unlock();
                }
            } finally {
                analyseReadLock.unlock();
            }
        }

        @Override
        public void tryAnalyseAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire) {
            if (analyseWriteLock.tryLock()) {
                try {
                    API.analyseAPI(ctx);
                } finally {
                    analyseWriteLock.unlock();
                }
            } else {
                onNonAcquire.handle(ctx);
            }
        }

        @Override
        public void trySchemaAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire) {
            if (analyseReadLock.tryLock()) {
                try {
                    API.schemaAPI(ctx);
                } finally {
                    analyseReadLock.unlock();
                }
            } else {
                onNonAcquire.handle(ctx);
            }
        }

        @Override
        public void tryStatusAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire) {
            if (analyseReadLock.tryLock()) {
                try {
                    API.statusAPI(ctx);
                } finally {
                    analyseReadLock.unlock();
                }
            } else {
                onNonAcquire.handle(ctx);
            }
        }

        @Override
        public void tryExecAPI(RoutingContext ctx, Handler<RoutingContext> onNonAcquire) {
            if (analyseReadLock.tryLock()) {
                if (execLock.tryLock()) {
                    try {
                        API.execAPI(ctx);
                    } finally {
                        execLock.unlock();
                        analyseReadLock.unlock();
                    }
                } else {
                    analyseReadLock.unlock();
                    onNonAcquire.handle(ctx);
                }
            } else {
                onNonAcquire.handle(ctx);
            }
        }
    }
}
