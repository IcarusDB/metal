package org.metal.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendDeployOptions<S> {

  @JsonProperty("confs")
  private Map<String, Object> confs;

  @JsonProperty("setups")
  private List<ISetup<S>> setups;

  @JsonCreator
  public BackendDeployOptions() {
    confs = new HashMap<>();
    setups = new ArrayList<>();
  }

  public Map<String, Object> getConfs() {
    return confs;
  }

  public List<ISetup<S>> getSetups() {
    return setups;
  }

  @Override
  public String toString() {
    return "BackendDeployOptions{" +
        "confs=" + confs +
        ", setups=" + setups +
        '}';
  }
}
