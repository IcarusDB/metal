package org.metal.backend.rest.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.metal.backend.api.BackendService;
import org.metal.backend.rest.IBackendRestEndApi;

public class BackendRestEndApiImpl implements IBackendRestEndApi {
  private BackendService backendService;

  @Override
  public void analyseAPI(RoutingContext ctx) {
    JsonObject resp = new JsonObject();
    JsonObject body = ctx.body().asJsonObject();
  }

  @Override
  public void schemaAPI(RoutingContext ctx) {

  }

  @Override
  public void heartAPI(RoutingContext ctx) {

  }

  @Override
  public void statusAPI(RoutingContext ctx) {

  }

  @Override
  public void execAPI(RoutingContext ctx) {

  }
}
