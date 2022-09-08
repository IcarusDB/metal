package org.metal.backend.rest;

import io.vertx.ext.web.RoutingContext;

public interface IBackendRestEndApi {
  public void analyseAPI(RoutingContext ctx);

  public void schemaAPI(RoutingContext ctx);

  public void heartAPI(RoutingContext ctx);

  public void statusAPI(RoutingContext ctx);

  public void execAPI(RoutingContext ctx);
}
