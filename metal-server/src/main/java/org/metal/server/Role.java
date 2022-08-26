package org.metal.server;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

public interface Role {
  public Map<String, String> conf();
  public String type();

  @Value.Immutable
  public static abstract class GuestRole implements Role {
    @Value.NonAttribute
    @Override
    public String type() {
      return "Guest";
    }

    public static GuestRole of(JsonObject json) {
      ImmutableGuestRole.Builder builder = ImmutableGuestRole.builder();
      for(Map.Entry<String, Object> entry: json.getMap().entrySet()) {
        builder.putConf(entry.getKey(), entry.getValue().toString());
      }
      return builder.build();
    }

    public static JsonObject json(GuestRole role) {
      JsonObject json = new JsonObject();
      JsonObject conf = new JsonObject();
      for(Map.Entry<String, String> entry: role.conf().entrySet()) {
        conf.put(entry.getKey(), entry.getValue());
      }
      json.put("type", role.type())
          .put("conf", conf);
      return json;
    }
  }

  @Value.Immutable
  public static abstract class AdminRole implements Role {
    @Value.NonAttribute
    @Override
    public String type() {
      return "Admin";
    }

    public static AdminRole of(JsonObject json) {
      ImmutableAdminRole.Builder builder = ImmutableAdminRole.builder();
      for(Map.Entry<String, Object> entry: json.getMap().entrySet()) {
        builder.putConf(entry.getKey(), entry.getValue().toString());
      }
      return builder.build();
    }

    public static JsonObject json(AdminRole role) {
      JsonObject json = new JsonObject();
      JsonObject conf = new JsonObject();
      for(Map.Entry<String, String> entry: role.conf().entrySet()) {
        conf.put(entry.getKey(), entry.getValue());
      }
      json.put("type", role.type())
          .put("conf", conf);
      return json;
    }
  }

  public static Optional<Role> of(JsonObject json) {
    switch (json.getString("type")) {
      case "Guest": {
        return Optional.<Role>of(GuestRole.of(json));
      }
      case "Admin": {
        return Optional.<Role>of(AdminRole.of(json));
      }
      default: {
        return Optional.<Role>empty();
      }
    }
  }

  public static JsonObject json(Role role) {
    switch (role.type()) {
      case "Guest": {
        return GuestRole.json((GuestRole) role);
      }
      case "Admin": {
        return AdminRole.json((AdminRole) role);
      }
      default: return new JsonObject();
    }
  }
}
