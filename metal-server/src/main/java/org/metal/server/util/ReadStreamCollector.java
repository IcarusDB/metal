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

package org.metal.server.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ReadStreamCollector {

  private final static Logger LOGGER = LoggerFactory.getLogger(ReadStreamCollector.class);

  public static <T> Future<List<T>> toList(ReadStream<T> stream) {
    return ReadStreamCollector.<T, T>toList(stream, (T t) -> {
      return t;
    });
  }

  public static <T, R> Future<List<R>> toList(ReadStream<T> stream, Function<T, R> map) {
    Promise<List<R>> promise = Promise.promise();
    List<R> stage = new ArrayList<>();
    stream.handler((T t) -> {
      R r = map.apply(t);
      stage.add(r);
    }).endHandler((Void r) -> {
      promise.complete(Collections.unmodifiableList(stage));
    }).exceptionHandler(error -> {
      promise.fail(error);
      LOGGER.error(error);
    });
    return promise.future();
  }
}
