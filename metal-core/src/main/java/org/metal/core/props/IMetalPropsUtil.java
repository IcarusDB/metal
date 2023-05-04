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

package org.metal.core.props;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IMetalPropsUtil {

  public static HashCode sha256(IMetalProps props) throws NullPointerException, IOException {
    Objects.requireNonNull(props);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    out.writeObject(props);
    return Hashing.sha256().hashBytes(buffer.toByteArray());
  }

  public static HashCode sha256WithPrev(IMetalProps props, List<HashCode> prevs)
      throws NullPointerException, IOException {
    List<HashCode> hashCodes = new ArrayList<>();
    hashCodes.addAll(prevs);
    hashCodes.add(sha256(props));
    return Hashing.combineOrdered(hashCodes);
  }
}
