package org.metal.backend.rest;

import io.vertx.ext.web.RoutingContext;
import org.metal.backend.api.BackendService;
import org.metal.backend.rest.impl.BackendRestEndApiImpl;

public interface IBackendRestEndApi {

  public static IBackendRestEndApi create(BackendService backendService) {
    return new BackendRestEndApiImpl(backendService);
  }

  public void analyseAPI(RoutingContext ctx);

  public void schemaAPI(RoutingContext ctx);

  public void heartAPI(RoutingContext ctx);

  public void statusAPI(RoutingContext ctx);

  public void execAPI(RoutingContext ctx);
}
