package org.metal.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class JsonTest {

  @Test
  public void test() {
    List<JsonObject> arr = List.of();
    Map<String, JsonObject> map = Map.of("k1", new JsonObject());
    System.out.println(
        JsonObject.mapFrom(map).toString()
    );

    System.out.println(
        JsonArray.of(arr.toArray())
    );

  }
}
