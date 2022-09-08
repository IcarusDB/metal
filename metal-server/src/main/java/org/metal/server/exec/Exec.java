package org.metal.server.exec;

import io.vertx.codegen.annotations.DataObject;
public class Exec {
  @DataObject(generateConverter = true, publicConverter = false)
  public static enum State {
    SUBMIT, RUNNING, FINISH, FAILURE
  }

  @DataObject(generateConverter = true, publicConverter = false)
  public static class Entry {
    private String id;
    private long submitTime;
    private long finishTime;
    private State state;
  }

}
