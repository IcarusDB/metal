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

package org.metal.core;

import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.immutables.value.Value;
import org.junit.Test;
import org.metal.core.props.IMMapperProps;
import org.metal.core.props.IMSinkProps;
import org.metal.core.props.IMSourceProps;
import org.metal.core.props.IMetalProps;

public class TraceIdTest {

  @Value.Immutable
  interface FooMSourceProps extends IMSourceProps {

  }

  @Value.Immutable
  interface FooMSinkProps extends IMSinkProps {

  }

  @Value.Immutable
  interface FooMMapperProps extends IMMapperProps {

  }

  private String traceId(IMetalProps props) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    out.writeObject(props);
    return Hashing.sha512().hashBytes(buffer.toByteArray()).toString();
  }

  @Test
  public void testTrace() {
    FooMSourceProps sourceProps0 = ImmutableFooMSourceProps.builder().schema("{}").build();
    FooMSourceProps sourceProps1 = ImmutableFooMSourceProps.builder().schema("").build();

    FooMSinkProps sinkProps0 = ImmutableFooMSinkProps.builder().build();
    FooMSinkProps sinkProps1 = ImmutableFooMSinkProps.builder().build();

    FooMMapperProps mapperProps0 = ImmutableFooMMapperProps.builder().build();
    FooMMapperProps mapperProps1 = ImmutableFooMMapperProps.builder().build();

    try {
      System.out.println(traceId(sourceProps0));
      System.out.println(traceId(sourceProps1));

      System.out.println(traceId(sinkProps0));
      System.out.println(traceId(sinkProps1));

      System.out.println(traceId(mapperProps0));
      System.out.println(traceId(mapperProps1));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
