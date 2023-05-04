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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;
import org.metal.core.props.IMFusionProps;
import org.metal.core.props.IMMapperProps;
import org.metal.core.props.IMSinkProps;
import org.metal.core.props.IMSourceProps;
import org.metal.exception.MetalTranslateException;

public class Mock {

  public static class MFusionImpl extends MFusion<String, Thread, MFusionPropsFoo> {

    @JsonCreator
    public MFusionImpl(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("props") MFusionPropsFoo props) {
      super(id, name, props);
    }

    @Override
    public String fusion(Thread platform, Map<String, String> datas) {
      return "I amm fusion";
    }
  }

  @Value.Immutable
  @JsonDeserialize(as = ImmutableMFusionPropsFoo.class)
  @JsonSerialize(as = ImmutableMFusionPropsFoo.class)
  public static
  interface MFusionPropsFoo extends IMFusionProps {

  }

  public static class MMapperImpl extends MMapper<String, Thread, MMapperPropsFoo> {

    @JsonCreator
    public MMapperImpl(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("props") MMapperPropsFoo props) {
      super(id, name, props);
    }

    @Override
    public String map(Thread platform, String data) {
      return "I am mapper";
    }
  }

  @Value.Immutable
  @JsonDeserialize(as = ImmutableMMapperPropsFoo.class)
  @JsonSerialize(as = ImmutableMMapperPropsFoo.class)
  public static
  interface MMapperPropsFoo extends IMMapperProps {

  }

  public static class MSinkImpl extends MSink<String, Thread, MSinkPropsFoo> {

    @JsonCreator
    public MSinkImpl(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("props") MSinkPropsFoo props) {
      super(id, name, props);
    }

    @Override
    public IMExecutor sink(Thread platform, String data) throws MetalTranslateException {
      return () -> {
        System.out.println("I am sink");
      };
    }
  }

  @Value.Immutable
  @JsonDeserialize(as = ImmutableMSinkPropsFoo.class)
  @JsonSerialize(as = ImmutableMSinkPropsFoo.class)
  public static
  interface MSinkPropsFoo extends IMSinkProps {

  }

  public static class MSourceImpl extends MSource<String, Thread, MSourcePropsFoo> {

    @JsonCreator
    public MSourceImpl(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("props") MSourcePropsFoo props) {
      super(id, name, props);
    }

    @Override
    public String source(Thread platform) {
      return "I am source.";
    }
  }

  @Value.Immutable
  @JsonDeserialize(as = ImmutableMSourcePropsFoo.class)
  @JsonSerialize(as = ImmutableMSourcePropsFoo.class)
  public static
  interface MSourcePropsFoo extends IMSourceProps {

  }
}
