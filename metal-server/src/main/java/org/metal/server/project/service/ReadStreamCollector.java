package org.metal.server.project.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.streams.ReadStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ReadStreamCollector {
  public static <T> Future<List<T>> toList(ReadStream<T> stream) {
    Promise<List<T>> promise = Promise.promise();
    List<T> stage = new ArrayList<>();
    stream.handler((T t) -> {
      stage.add(t);
    }).endHandler((Void r) -> {
      promise.complete(Collections.unmodifiableList(stage));
    }).exceptionHandler(error -> {
      promise.fail(error);
    });

    return promise.future();
  }
}
