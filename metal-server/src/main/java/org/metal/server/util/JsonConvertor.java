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

package org.metal.server.util;

import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class JsonConvertor {

    public static List<String> jsonArrayToList(JsonArray array) {
        List<String> ret = new ArrayList<>();
        if (array == null) {
            return ret;
        }

        if (array != null) {
            for (int idx = 0; idx < array.size(); idx++) {
                String e = array.getString(idx);
                if (e != null && !e.isBlank()) {
                    ret.add(e);
                }
            }
        }
        return ret;
    }
}
