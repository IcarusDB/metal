package org.metal.server.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadStreamCollector {
  private final static Logger LOGGER = LoggerFactory.getLogger(ReadStreamCollector.class);

  public static <T> Future<List<T>> toList(ReadStream<T> stream) {
    Promise<List<T>> promise = Promise.promise();
    List<T> stage = new ArrayList<>();
    stream.handler((T t) -> {
      stage.add(t);
    }).endHandler((Void r) -> {
      promise.complete(Collections.unmodifiableList(stage));
    }).exceptionHandler(error -> {
      promise.fail(error);
      LOGGER.error(error);
    });
    return promise.future();
  }
}
