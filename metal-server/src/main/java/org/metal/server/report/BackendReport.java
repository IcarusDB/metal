/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.server.report;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.metal.server.api.BackendReportService;
import org.metal.server.exec.ExecService;
import org.metal.server.project.service.IProjectService;
import org.metal.server.report.api.impl.BackendReportServiceImpl;

public class BackendReport extends AbstractVerticle {

  public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
  public static final String MONGO_CONF = "mongoConf";

  public static final String BACKEND_REPORT_CONF = "backendReport";
  public static final String PROJECT_SERVICE_CONF = "projectService";
  public static final String PROJECT_SERVICE_ADDRESS_CONF = "address";
  public static final String EXEC_SERVICE_CONF = "execService";
  public static final String EXEC_SERVICE_ADDRESS_CONF = "address";

  public static final String BACKEND_REPORT_SERVICE_CONF = "backendReportService";
  public static final String BACKEND_REPORT_SERVICE_ADDRESS_CONF = "address";

  private ExecService execService;
  private IProjectService projectService;
  private BackendReportService report;
  private MessageConsumer<JsonObject> consumer;

  private BackendReport() {
  }

  public static BackendReport create() {
    return new BackendReport();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ConfigStoreOptions fileConfigStoreOptions = new ConfigStoreOptions()
        .setType("file")
        .setConfig(new JsonObject().put("path", CONF_METAL_SERVER_PATH))
        .setOptional(true);

    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
        .addStore(fileConfigStoreOptions);
    ConfigRetriever retriever = ConfigRetriever.create(getVertx(), retrieverOptions);
    retriever.getConfig().compose((JsonObject conf) -> {
      JsonObject backendReportConf = conf.getJsonObject(BACKEND_REPORT_CONF);
      JsonObject execServiceConf = backendReportConf.getJsonObject(EXEC_SERVICE_CONF);
      JsonObject projectServiceConf = backendReportConf.getJsonObject(PROJECT_SERVICE_CONF);
      JsonObject backendReportServiceConf = backendReportConf.getJsonObject(
          BACKEND_REPORT_SERVICE_CONF);
      String backendReportServiceAddress = backendReportServiceConf.getString(
          BACKEND_REPORT_SERVICE_ADDRESS_CONF);

      if (execServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", EXEC_SERVICE_CONF,
            CONF_METAL_SERVER_PATH));
      }
      if (projectServiceConf == null) {
        return Future.failedFuture(
            String.format("%s is not configured in %s.", PROJECT_SERVICE_CONF,
                CONF_METAL_SERVER_PATH));
      }
      if (backendReportServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", BACKEND_REPORT_CONF,
            CONF_METAL_SERVER_PATH));
      }
      if (backendReportServiceAddress == null || backendReportServiceAddress.isBlank()) {
        return Future.failedFuture(
            String.format("%s is not configured in %s.", BACKEND_REPORT_SERVICE_ADDRESS_CONF,
                CONF_METAL_SERVER_PATH + "." + BACKEND_REPORT_CONF));
      }

      execService = ExecService.create(getVertx(), execServiceConf);
      projectService = IProjectService.create(getVertx(), projectServiceConf);
      report = new BackendReportServiceImpl(execService, projectService);
      ServiceBinder binder = new ServiceBinder(getVertx());
      binder.setAddress(backendReportServiceAddress);
      consumer = binder.register(BackendReportService.class, report);

      return Future.succeededFuture();
    }).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure(error -> {
      startPromise.fail(error);
    });
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
