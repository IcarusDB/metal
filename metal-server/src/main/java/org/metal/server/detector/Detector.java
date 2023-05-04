package org.metal.server.detector;

import static org.metal.server.project.service.ProjectDB.backendStatusCreateTimePath;
import static org.metal.server.project.service.ProjectDB.backendStatusCurrentPath;
import static org.metal.server.project.service.ProjectDB.backendStatusUpTimePath;
import static org.metal.server.project.service.ProjectDB.getTime;
import static org.metal.server.project.service.ProjectServiceImpl.backendAddress;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import org.metal.backend.api.BackendService;
import org.metal.server.api.BackendState;
import org.metal.server.project.service.ProjectDB;

public class Detector extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(Detector.class);
  public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
  public static final String MONGO_CONF = "mongoConf";
  public static final String DETECTOR_CONF = "detector";
  private MongoClient mongo;
  private long timerID = -1;
  private long detectorStartTime;
  private long detectorDuration = 15000l;
  private long inActiveDelay = 2 * detectorDuration;

  private long evictedDelay = 3 * detectorDuration;

  private Detector() {
  }

  public static Detector create() {
    return new Detector();
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
      JsonObject mongoConf = conf.getJsonObject(MONGO_CONF);
      JsonObject detectorConf = conf.getJsonObject(DETECTOR_CONF);

      if (mongoConf == null) {
        return Future.failedFuture(
            String.format("%s is not configured in %s.", MONGO_CONF, CONF_METAL_SERVER_PATH));
      }
      this.mongo = MongoClient.createShared(getVertx(), mongoConf);
      this.detectorStartTime = getTime();
      this.timerID = getVertx().setPeriodic(detectorDuration, this::detect);
      return Future.succeededFuture();
    }).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure(error -> {
      getVertx().cancelTimer(this.timerID);
      startPromise.fail(error);
    });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    getVertx().cancelTimer(this.timerID);
    this.mongo.close().onSuccess(ret -> {
      stopPromise.complete();
    }).onFailure(error -> {
      stopPromise.fail(error);
    });
  }


  private JsonObject deployIsInActive() {
    long inActiveTime = getTime() - this.inActiveDelay;
    return new JsonObject().put("$or",
        new JsonArray()
            .add(new JsonObject()
                .put(backendStatusCurrentPath(), BackendState.CREATED.toString())
                .put(backendStatusCreateTimePath(), new JsonObject().put("$lt", inActiveTime))
            )
            .add(new JsonObject()
                .put(backendStatusCurrentPath(), BackendState.UP.toString())
                .put(backendStatusUpTimePath(), new JsonObject().put("$lt", inActiveTime))
            )
    );
  }

  public void detect(long ID) {
    JsonObject matcher = deployIsInActive();
    ProjectDB.getAllOfMatcher(mongo, matcher).compose((List<JsonObject> inActiveProjects) -> {
      long evictedTime = getTime() - evictedDelay;
      for (JsonObject proj : inActiveProjects) {
        try {
          JsonObject deploy = proj.getJsonObject(ProjectDB.DEPLOY);
          String deployId = deploy.getString(ProjectDB.DEPLOY_ID);
          int epoch = deploy.getInteger(ProjectDB.DEPLOY_EPOCH);
          JsonObject backend = deploy.getJsonObject(ProjectDB.DEPLOY_BACKEND);
          JsonObject status = backend.getJsonObject(ProjectDB.DEPLOY_BACKEND_STATUS);
          BackendState current = BackendState.valueOf(status.getString("current"));
          if (current.equals(BackendState.CREATED)) {
            long createdTime = status.getLong(ProjectDB.DEPLOY_BACKEND_STATUS_CREATED_TIME);
            if (createdTime < detectorStartTime) {
              heart(deploy, deployId, epoch, current);
              continue;
            }
            if (createdTime < evictedTime) {
              String failureMsg = String.format(
                  "Backend[%s-%d] is %s state at %d, and has meet evicted condition[%d < %d].",
                  deployId, epoch, current.toString(), createdTime, createdTime, evictedTime);
              ProjectDB.updateBackendStatusOnFailure(mongo, deployId, epoch, current, failureMsg)
                  .onSuccess(success -> {
                    String msg = String.format("Backend[%s-%d] is up.", deployId, epoch);
                    LOGGER.info(msg);
                  })
                  .onFailure(error -> {
                    LOGGER.error(error);
                  });
              continue;
            }
          }

          if (current.equals(BackendState.UP)) {
            long upTime = status.getLong(ProjectDB.DEPLOY_BACKEND_STATUS_UP_TIME);
            if (upTime < detectorStartTime) {
              heart(deploy, deployId, epoch, current);
            }
            if (upTime < evictedTime) {
              String failureMsg = String.format(
                  "Backend[%s-%d] is %s state at %d, and has meet evicted condition[%d < %d].",
                  deployId, epoch, current.toString(), upTime, upTime, evictedTime);
              ProjectDB.updateBackendStatusOnFailure(mongo, deployId, epoch, current, failureMsg)
                  .onSuccess(success -> {
                    String msg = String.format("Backend[%s-%d] is failure. Failure message: %s.",
                        deployId, epoch, failureMsg);
                    LOGGER.info(msg);
                  })
                  .onFailure(error -> {
                    LOGGER.error(error);
                  });
              continue;
            }
          }
          heart(deploy, deployId, epoch, current);
        } catch (Exception e) {
          LOGGER.error(e);
        }
      }
      return Future.succeededFuture();
    });
  }

  private void heart(JsonObject deploy, String deployId, int epoch, BackendState current) {
    JsonObject address = backendAddress(deploy);
    BackendService backendService = BackendService.create(getVertx(), address);
    backendService.heart().onSuccess(ret -> {
      ProjectDB.updateBackendStatusOnUp(mongo, deployId, epoch, current)
          .onSuccess(success -> {
            String msg = String.format("Backend[%s-%d] is up.", deployId, epoch);
            LOGGER.info(msg);
          })
          .onFailure(error -> {
            LOGGER.error(error);
          });
    }).onFailure(error -> {
      LOGGER.error(error);
    });
  }
}
