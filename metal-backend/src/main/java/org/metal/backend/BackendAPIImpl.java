package org.metal.backend;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.commons.lang3.time.CalendarUtils;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalDraftException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;
import org.metal.exception.MetalSpecParseException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

public class BackendAPIImpl implements IBackendAPI{
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendAPIImpl.class);

  private IBackend backend;

  public BackendAPIImpl(IBackend backend) {
    this.backend = backend;
  }

  @Override
  public void analyseAPI(RoutingContext ctx) {
    JsonObject resp = new JsonObject();
    JsonObject body = ctx.body().asJsonObject();
    try {
      Spec spec = new SpecFactoryOnJson().get(body.toString());
      Draft draft = DraftMaster.draft(spec);
      backend.service().analyse(draft);
      List<String> analysed = backend.service().analysed();
      List<String> unAnalysed = backend.service().unAnalysed();
      resp.put("status", "OK")
          .put("data", new JsonObject()
              .put("analysed", analysed)
              .put("unAnalysed", unAnalysed)
          );
    } catch (MetalSpecParseException e) {
      LOGGER.error(e);
      resp.put("status", "FAIL")
          .put("msg", "Fail to parse spec into one Spec object. Spec: " + body.toString() + ".");
    } catch (MetalDraftException e) {
      LOGGER.error(e);
      resp.put("status", "FAIL")
          .put("msg", "Fail to create one Draft from Spec.");
    } catch (MetalAnalysedException e) {
      LOGGER.error(e);
      resp.put("status", "FAIL")
          .put("msg", "Fail to analyse metal.");
    } finally {
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    }
  }

  @Override
  public void schemaAPI(RoutingContext ctx) {
    String mid = ctx.pathParam("mid");
    JsonObject resp = new JsonObject();
    try {
      Schema schema = backend.service().schema(mid);
      resp.put("status", "OK")
          .put("data", new JsonObject()
              .put("id", mid)
              .put("schema", new JsonObject(schema.toJson()))
          );
    } catch (MetalServiceException e) {
      LOGGER.error(e);
      resp.put("status", "FAIL")
          .put("msg", "Fail to found schema");
    } finally {
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    }
  }

  @Override
  public void heartAPI(RoutingContext ctx) {
    JsonObject resp = new JsonObject();
    resp.put("status", "OK")
        .put("time", LocalDateTime.now());
    String payload = resp.toString();
    ctx.response()
        .putHeader("content-type", ctx.getAcceptableContentType())
        .putHeader("content-length", String.valueOf(payload.length()))
        .end(payload);
  }

  @Override
  public void statusAPI(RoutingContext ctx) {

  }

  @Override
  public void execAPI(RoutingContext ctx) {
    JsonObject resp = new JsonObject();
    boolean isExecReady = true;
    if (backend.service().analysed().isEmpty()) {
      isExecReady = false;
      resp.put("status", "FAIL")
          .put("msg", "Not any analysed metal in context.");
    } else if (!backend.service().unAnalysed().isEmpty()) {
      isExecReady = false;
      resp.put("status", "FAIL")
          .put("msg", "Some unAnalysed metals exist in context.");
    }

    if (isExecReady) {
      resp.put("status", "OK")
          .put("msg", "Success to execute.");
    }

    String payload = resp.toString();
    ctx.response()
        .putHeader("content-type", ctx.getAcceptableContentType())
        .putHeader("content-length", String.valueOf(payload.length()))
        .end(payload);

    if (isExecReady) {
      try {
        backend.service().exec();
        /***
         * Report Finish.
         * TODO
         */

      } catch (MetalExecuteException e) {
        /***
         * Report Exception.
         * TODO
         */
        LOGGER.error(e);
      }
    }
  }
}
