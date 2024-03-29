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

package org.metal.server.auth;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class AttachRoles {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachRoles.class);
    private MongoClient mongo;

    private AttachRoles(MongoClient client) {
        this.mongo = client;
    }

    public static AttachRoles create(MongoClient client) {
        return new AttachRoles(client);
    }

    public Future<User> roles(User user) {
        if (user == null) {
            LOGGER.warn("Fail to attach any roles for user, because no user has been set.");
            return Future.failedFuture(
                    new NullPointerException(
                            "Fail to attach any roles for user, because user is null."));
        }

        return mongo.findOne(
                        "user",
                        new JsonObject().put("username", user.get("username")),
                        new JsonObject().put("roles", true))
                .compose(
                        json -> {
                            try {
                                JsonArray roles = json.getJsonArray("roles");
                                for (int idx = 0; idx < roles.size(); idx++) {
                                    RoleBasedAuthorization authorization =
                                            RoleBasedAuthorization.create(roles.getString(idx));
                                    user.authorizations()
                                            .add(authorization.getRole(), authorization);
                                }
                                return Future.succeededFuture(user);
                            } catch (NullPointerException error) {
                                LOGGER.error("Fail to attach any roles for user.", error);
                                return Future.failedFuture(error);
                            }
                        });
    }

    public void attach(RoutingContext ctx) {
        User user = ctx.user();
        if (user == null) {
            LOGGER.warn("Fail to attach any roles for user, because no user has been set.");
        }

        mongo.findOne(
                        "user",
                        new JsonObject().put("username", user.get("username")),
                        new JsonObject().put("roles", true))
                .onSuccess(
                        json -> {
                            try {
                                JsonArray roles = json.getJsonArray("roles");
                                for (int idx = 0; idx < roles.size(); idx++) {
                                    RoleBasedAuthorization authorization =
                                            RoleBasedAuthorization.create(roles.getString(idx));
                                    user.authorizations()
                                            .add(authorization.getRole(), authorization);
                                }
                                ctx.setUser(user);
                            } catch (NullPointerException error) {
                                LOGGER.error("Fail to attach any roles for user.", error);
                            } finally {
                                ctx.next();
                            }
                        })
                .onFailure(
                        error -> {
                            LOGGER.error("Fail to attach any roles for user.", error);
                            LOGGER.error(error);
                            ctx.next();
                        });
    }
}
