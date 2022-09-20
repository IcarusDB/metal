package org.metal.server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class StreamTest {
  @Test
  public void test() {
    Stream<Integer> s = Arrays.stream(new int[]{1, 2, 4})
        .boxed();
    s.collect(Collectors.toList());
  }

}
