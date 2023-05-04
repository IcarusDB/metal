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

package org.metal.draft;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Objects;
import org.metal.core.MSink;
import org.metal.core.Metal;
import org.metal.core.Pair;
import org.metal.exception.MetalDraftException;
import org.metal.specs.Spec;

public class DraftMaster {

  public static Draft draft(Spec spec) throws MetalDraftException {
    try {
      spec = Objects.requireNonNull(spec);
    } catch (NullPointerException e) {
      throw new MetalDraftException(e);
    }

    BiMap<String, Metal> id2Metals = HashBiMap.create(spec.getMetals().size());
    Draft.Builder builder = Draft.builder();

    spec.getMetals().stream().forEach(builder::add);
    spec.getMetals().stream().map((metal) -> {
      return Pair.of(metal.id(), metal);
    }).forEach(pair -> {
      id2Metals.put(pair.left(), pair.right());
    });

    if (id2Metals.size() != spec.getMetals().size()) {
      String msg = String.format("Metals in spec maybe duplicated.");
      throw new MetalDraftException(msg);
    }

    spec.getEdges().stream().map(pair -> {
      return Pair.of(id2Metals.get(pair.left()), id2Metals.get(pair.right()));
    }).forEach(pair -> {
      builder.addEdge(pair.left(), pair.right());
    });

    Draft.WithWaitFor withWait = builder.withWait();

    for (Pair<String, String> wait : spec.getWaitFor()) {
      Metal metal = id2Metals.get(wait.right());
      if (!(metal instanceof MSink)) {
        String msg = String.format("Metal{%s} is must be MSink.", wait.right());
        throw new MetalDraftException(msg);
      }
      MSink mSink = (MSink) metal;
      Metal affectedMetal = id2Metals.get(wait.left());
      withWait.waitFor(affectedMetal, mSink);
    }

    return withWait.build();
  }
}
