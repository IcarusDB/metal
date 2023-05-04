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
