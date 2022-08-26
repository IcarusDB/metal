package org.metal.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface User {
  public Optional<String> id();
  public String name();
  public String pwd();
  public List<Role> roles();

  public static Optional<User> of(JsonObject json) {
    try {
      ImmutableUser.Builder builder = ImmutableUser.builder()
          .id(Optional.ofNullable(json.getString("_id")))
          .name(json.getString("name"))
          .pwd(json.getString("pwd"));

      JsonArray roles = json.getJsonArray("roles");
      for(int idx = 0; idx < roles.size(); idx++) {
        Optional<Role> role = Role.of(roles.getJsonObject(idx));
        role.ifPresent(r -> builder.addRoles(r));
      }
      return Optional.of(builder.build());
    } catch (Exception e) {
      return Optional.<User>empty();
    }
  }

  public static JsonObject json(User user) {
    JsonObject json = new JsonObject();
    user.id().ifPresent(id -> json.put("_id", id));
    json.put("name", user.name())
        .put("pwd", user.pwd());

    JsonArray roles = new JsonArray();
    user.roles().forEach(role -> {
      roles.add(Role.json(role));
    });
    json.put("roles", roles);
    return json;
  }
}
