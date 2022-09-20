package org.metal.server.project.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.serviceproxy.HelperUtils;
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
      System.out.println("RET:" + t.toString());
    }).endHandler((Void r) -> {
      promise.complete(Collections.unmodifiableList(stage));
    }).exceptionHandler(error -> {
      promise.fail(error);
      error.printStackTrace();
    });

    return promise.future();
  }

  public static <T> Future<List<T>> toList(VertxInternal vertx, ReadStream<T> stream) {
//    Promise<List<T>> promise = vertx.promise();
    Promise<List<T>> promise = vertx.getOrCreateContext().promise();
    List<T> stage = new ArrayList<>();
    stream.handler((T t) -> {
      stage.add(t);
      System.out.println("RET:" + t.toString());
    }).endHandler((Void r) -> {
      System.out.println("END:" + stage);
//      promise.complete();
      promise.handle(new AsyncResult<List<T>>() {
        @Override
        public List<T> result() {
          return stage;
        }

        @Override
        public Throwable cause() {
          return null;
        }

        @Override
        public boolean succeeded() {
          return true;
        }

        @Override
        public boolean failed() {
          return false;
        }
      });
//      promise.complete(Collections.unmodifiableList(stage));
    }).exceptionHandler(error -> {
      promise.fail(error);
      error.printStackTrace();
    });


    return promise
        .future();
  }
}
