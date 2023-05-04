package org.metal.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class Pair<L, R> implements Serializable {

  @JsonProperty
  private L left;

  @JsonProperty
  private R right;

  @JsonCreator
  public static <L, R> Pair<L, R> of(@JsonProperty("left") L left, @JsonProperty("right") R right) {
    return new Pair<L, R>(left, right);
  }

  private Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L left() {
    return left;
  }

  public R right() {
    return right;
  }

}
