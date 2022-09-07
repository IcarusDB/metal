package org.metal.backend.api.impl;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.arrow.vector.types.pojo.Schema;
import org.metal.backend.IBackend;
import org.metal.backend.api.BackendService;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalDraftException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;
import org.metal.exception.MetalSpecParseException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

public class BackendServiceImpl implements BackendService {
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendServiceImpl.class);
  private IBackend backend;

  @Override
  public Future<JsonObject> analyse(JsonObject spec) {
    try {
      Spec specObj = new SpecFactoryOnJson().get(spec.toString());
      Draft draft = DraftMaster.draft(specObj);
      backend.service().analyse(draft);
      List<String> analysed = backend.service().analysed();
      List<String> unAnalysed = backend.service().unAnalysed();
      JsonObject resp = new JsonObject();
      resp.put("analysed", analysed)
          .put("unAnalysed", unAnalysed);
      return Future.succeededFuture(resp);
    } catch (MetalSpecParseException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    } catch (MetalDraftException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    } catch (MetalAnalysedException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> schemaAPI(String metalId) {
    try {
      Schema schema = backend.service().schema(metalId);
      JsonObject resp = new JsonObject();
      resp.put("id", metalId)
          .put("schema", new JsonObject(schema.toJson()));
      return Future.succeededFuture(resp);
    } catch (MetalServiceException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> heartAPI() {
    JsonObject resp = new JsonObject();
    resp.put("time", LocalDateTime.now());
    return Future.succeededFuture(resp);
  }

  @Override
  public Future<JsonObject> statusAPI() {
    return Future.succeededFuture();
  }

  @Override
  public Future<JsonObject> execAPI() {
    if (backend.service().analysed().isEmpty()) {
      return Future.failedFuture("Not any analysed metal in context.");
    }

    if (!backend.service().unAnalysed().isEmpty()) {
      return Future.failedFuture("Some unAnalysed metals exist in context.");
    }

    try {
      backend.service().exec();
      return Future.succeededFuture();
    } catch (MetalExecuteException e) {
      return Future.failedFuture(e);
    }
  }
}
