package org.metal.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.metal.server.util.JsonKeyReplacer;

public class JsonKeyReplacerTest {
  @Test
  public void test0() {
    JsonObject json = new JsonObject();
    json.put("spark.o.version", "1.0");
    json.put("conf", new JsonObject().put("spark.k.v", new JsonArray()
        .add(new JsonArray().add(1))
        .add(new JsonObject().put("spark.k.v", 2).put("spark.k1.v1", 4))));
    System.out.println(
        json.encodePrettily()
    );
    System.out.println(
        JsonKeyReplacer.compatBson(json).encodePrettily()
    );
    System.out.println(
        JsonKeyReplacer.compatJson(
            JsonKeyReplacer.compatBson(json)
        ).encodePrettily()
    );

  }


}
