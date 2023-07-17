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

package org.metal.server.detector;

import org.metal.backend.api.BackendService;
import org.metal.server.api.BackendState;
import org.metal.server.project.service.ProjectDB;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

import static org.metal.server.project.service.ProjectDB.backendStatusCreateTimePath;
import static org.metal.server.project.service.ProjectDB.backendStatusCurrentPath;
import static org.metal.server.project.service.ProjectDB.backendStatusUpTimePath;
import static org.metal.server.project.service.ProjectDB.getTime;
import static org.metal.server.project.service.ProjectServiceImpl.backendAddress;

public class Detector extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);
    public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
    public static final String MONGO_CONF = "mongoConf";
    public static final String DETECTOR_CONF = "detector";
    private MongoClient mongo;
    private long timerID = -1;
    private long detectorStartTime;
    private long detectorDuration = 15000l;
    private long inActiveDelay = 2 * detectorDuration;

    private long evictedDelay = 3 * detectorDuration;

    private Detector() {}

    public static Detector create() {
        return new Detector();
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        ConfigStoreOptions fileConfigStoreOptions =
                new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", CONF_METAL_SERVER_PATH))
                        .setOptional(true);

        ConfigRetrieverOptions retrieverOptions =
                new ConfigRetrieverOptions().addStore(fileConfigStoreOptions);
        ConfigRetriever retriever = ConfigRetriever.create(getVertx(), retrieverOptions);
        retriever
                .getConfig()
                .compose(
                        (JsonObject conf) -> {
                            JsonObject mongoConf = conf.getJsonObject(MONGO_CONF);
                            JsonObject detectorConf = conf.getJsonObject(DETECTOR_CONF);

                            if (mongoConf == null) {
                                return Future.failedFuture(
                                        String.format(
                                                "%s is not configured in %s.",
                                                MONGO_CONF, CONF_METAL_SERVER_PATH));
                            }
                            this.mongo = MongoClient.createShared(getVertx(), mongoConf);
                            this.detectorStartTime = getTime();
                            this.timerID = getVertx().setPeriodic(detectorDuration, this::detect);
                            return Future.succeededFuture();
                        })
                .onSuccess(
                        ret -> {
                            startPromise.complete();
                        })
                .onFailure(
                        error -> {
                            getVertx().cancelTimer(this.timerID);
                            startPromise.fail(error);
                        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        getVertx().cancelTimer(this.timerID);
        this.mongo
                .close()
                .onSuccess(
                        ret -> {
                            stopPromise.complete();
                        })
                .onFailure(
                        error -> {
                            stopPromise.fail(error);
                        });
    }

    private JsonObject deployIsInActive() {
        long inActiveTime = getTime() - this.inActiveDelay;
        return new JsonObject()
                .put(
                        "$or",
                        new JsonArray()
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.CREATED.toString())
                                                .put(
                                                        backendStatusCreateTimePath(),
                                                        new JsonObject().put("$lt", inActiveTime)))
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.UP.toString())
                                                .put(
                                                        backendStatusUpTimePath(),
                                                        new JsonObject()
                                                                .put("$lt", inActiveTime))));
    }

    public void detect(long ID) {
        JsonObject matcher = deployIsInActive();
        ProjectDB.getAllOfMatcher(mongo, matcher)
                .compose(
                        (List<JsonObject> inActiveProjects) -> {
                            long evictedTime = getTime() - evictedDelay;
                            long beforeStartEvictedTime = 2 * evictedTime;
                            for (JsonObject proj : inActiveProjects) {
                                try {
                                    JsonObject deploy = proj.getJsonObject(ProjectDB.DEPLOY);
                                    String deployId = deploy.getString(ProjectDB.DEPLOY_ID);
                                    int epoch = deploy.getInteger(ProjectDB.DEPLOY_EPOCH);
                                    JsonObject backend =
                                            deploy.getJsonObject(ProjectDB.DEPLOY_BACKEND);
                                    JsonObject status =
                                            backend.getJsonObject(ProjectDB.DEPLOY_BACKEND_STATUS);
                                    BackendState current =
                                            BackendState.valueOf(status.getString("current"));
                                    LOGGER.info(
                                            String.format(
                                                    "Backend[%s-%d] recent status is %s",
                                                    deployId, epoch, current.toString()));

                                    if (current.equals(BackendState.CREATED)) {
                                        long createdTime =
                                                status.getLong(
                                                        ProjectDB
                                                                .DEPLOY_BACKEND_STATUS_CREATED_TIME);
                                        LOGGER.info(
                                                String.format(
                                                        "Backend[%s-%d] create time: %d, detector start time: %d",
                                                        deployId,
                                                        epoch,
                                                        createdTime,
                                                        detectorStartTime));
                                        if (createdTime < detectorStartTime) {
                                            LOGGER.info(
                                                    String.format(
                                                            "Send heart beat to Backend[%s-%d]",
                                                            deployId, epoch));
                                            heart(
                                                    deploy,
                                                    deployId,
                                                    epoch,
                                                    current,
                                                    (error) -> {
                                                        String failureMsg =
                                                                String.format(
                                                                        "Backend[%s-%d] status is %s, create at %d before detector started. Fail to send heart beat to it.",
                                                                        deployId,
                                                                        epoch,
                                                                        current.toString(),
                                                                        createdTime);
                                                        LOGGER.info(failureMsg);
                                                        updateBackendStatusToFailure(
                                                                deployId,
                                                                epoch,
                                                                current,
                                                                failureMsg);
                                                    });
                                            continue;
                                        }
                                        maybeEvicted(
                                                evictedTime,
                                                deploy,
                                                deployId,
                                                epoch,
                                                current,
                                                createdTime);
                                    }

                                    if (current.equals(BackendState.UP)) {
                                        long upTime =
                                                status.getLong(
                                                        ProjectDB.DEPLOY_BACKEND_STATUS_UP_TIME);
                                        LOGGER.info(
                                                String.format(
                                                        "Backend[%s-%d] up time: %d, detector start time: %d",
                                                        deployId,
                                                        epoch,
                                                        upTime,
                                                        detectorStartTime));
                                        if (upTime < detectorStartTime) {
                                            LOGGER.info(
                                                    String.format(
                                                            "Send heart beat to Backend[%s-%d]",
                                                            deployId, epoch));
                                            heart(
                                                    deploy,
                                                    deployId,
                                                    epoch,
                                                    current,
                                                    (error) -> {
                                                        String failureMsg =
                                                                String.format(
                                                                        "Backend[%s-%d] status is %s, up at %d before detector started. Fail to send heart beat to it.",
                                                                        deployId,
                                                                        epoch,
                                                                        current.toString(),
                                                                        upTime);
                                                        LOGGER.info(failureMsg);
                                                        updateBackendStatusToFailure(
                                                                deployId,
                                                                epoch,
                                                                current,
                                                                failureMsg);
                                                    });
                                            continue;
                                        }
                                        maybeEvicted(
                                                evictedTime,
                                                deploy,
                                                deployId,
                                                epoch,
                                                current,
                                                upTime);
                                    }
                                } catch (Exception e) {
                                    LOGGER.error(e);
                                }
                            }
                            return Future.succeededFuture();
                        });
    }

    private void maybeEvicted(
            long evictedTime,
            JsonObject deploy,
            String deployId,
            int epoch,
            BackendState current,
            long currentStatusTime) {
        if (currentStatusTime < evictedTime) {
            String failureMsg =
                    String.format(
                            "Backend[%s-%d] is %s state at %d, and has meet evicted condition[%d < %d].",
                            deployId,
                            epoch,
                            current.toString(),
                            currentStatusTime,
                            currentStatusTime,
                            evictedTime);
            LOGGER.info(failureMsg);
            updateBackendStatusToFailure(deployId, epoch, current, failureMsg);
        } else {
            heart(deploy, deployId, epoch, current);
        }
    }

    private void updateBackendStatusToFailure(
            String deployId, int epoch, BackendState current, String failureMsg) {
        ProjectDB.updateBackendStatusOnFailure(mongo, deployId, epoch, current, failureMsg)
                .onSuccess(
                        success -> {
                            String msg =
                                    String.format(
                                            "Update Backend[%s-%d] status to %s.",
                                            deployId, epoch, BackendState.FAILURE.toString());
                            LOGGER.info(msg);
                        })
                .onFailure(
                        error -> {
                            LOGGER.error(
                                    String.format(
                                            "Fail to update Backend[%s-%d] status to %s",
                                            deployId, epoch, BackendState.FAILURE.toString()));
                            LOGGER.error(error);
                        });
    }

    private void heart(JsonObject deploy, String deployId, int epoch, BackendState current) {
        heart(deploy, deployId, epoch, current, (error) -> {});
    }

    private void heart(
            JsonObject deploy,
            String deployId,
            int epoch,
            BackendState current,
            Handler<Throwable> onFailure) {
        JsonObject address = backendAddress(deploy);
        BackendService backendService = BackendService.create(getVertx(), address);
        backendService
                .heart()
                .onSuccess(
                        ret -> {
                            ProjectDB.updateBackendStatusOnUp(mongo, deployId, epoch, current)
                                    .onSuccess(
                                            success -> {
                                                String msg =
                                                        String.format(
                                                                "Backend[%s-%d] is up.",
                                                                deployId, epoch);
                                                LOGGER.info(msg);
                                            })
                                    .onFailure(
                                            error -> {
                                                onFailure.handle(error);
                                                LOGGER.error(
                                                        String.format(
                                                                "Fail to update Backend[%s-%d] status to %s.",
                                                                deployId,
                                                                epoch,
                                                                BackendState.UP.toString()));
                                                LOGGER.error(error);
                                            });
                        })
                .onFailure(
                        error -> {
                            onFailure.handle(error);
                            LOGGER.error(
                                    String.format("Fail to heart Backend[%s-%d]", deployId, epoch));
                            LOGGER.error(error);
                        });
    }
}
