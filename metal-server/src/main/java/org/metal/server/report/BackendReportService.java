package org.metal.server.report;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.metal.server.api.BackendReport;
import org.metal.server.exec.ExecService;
import org.metal.server.project.service.IProjectService;
import org.metal.server.report.api.impl.BackendReportImpl;

public class BackendReportService extends AbstractVerticle {
  private ExecService execService;
  private IProjectService projectService;
  private BackendReport report;
  private MessageConsumer<JsonObject> consumer;

  private BackendReportService() {}

  public static BackendReportService create() {
    return new BackendReportService();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    JsonObject execServiceConf = config().getJsonObject("execService");
    JsonObject projectServiceConf = config().getJsonObject("projectService");
    JsonObject backendReportServiceConf = config().getJsonObject("backendReportService");

    execService = ExecService.create(getVertx(), execServiceConf);
    projectService = IProjectService.create(getVertx(), projectServiceConf);
    report = new BackendReportImpl(execService, projectService);
    ServiceBinder binder = new ServiceBinder(getVertx());
    String address = backendReportServiceConf.getString("address");
    binder.setAddress(address);
    consumer = binder.register(BackendReport.class, report);
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    consumer.unregister().onSuccess(ret -> {
      stopPromise.complete();
    }).onFailure(error -> {
      stopPromise.fail(error);
    });
  }
}
