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

package org.metal.specs;

import java.util.ArrayList;
import java.util.List;
import org.metal.core.Metal;
import org.metal.core.Pair;

public class Spec {

  public final static String VERSION = "1.0";
  private String version;
  private List<Metal> metals;
  private List<Pair<String, String>> edges;

  /**
   * Left : affected metal id Right : metal id which need to be waited.
   */
  private List<Pair<String, String>> waitFor;

  public Spec() {
    this.waitFor = new ArrayList<>();
  }

  public Spec(String version) {
    this.version = version;
    this.metals = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.waitFor = new ArrayList<>();
  }

  public String getVersion() {
    return version;
  }

  public List<Metal> getMetals() {
    return metals;
  }

  public List<Pair<String, String>> getEdges() {
    return edges;
  }

  public List<Pair<String, String>> getWaitFor() {
    return waitFor;
  }
}
