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

package org.metal.backend.spark.extension.ml;

public class Convertor {

    public static double[] flat2DArray(Double[][] v) {
        double[] values = new double[v.length * v[0].length];
        for (int row = 0, start = 0; row < v.length; row++, start += v[0].length) {
            for (int col = 0; col < v[0].length; col++) {
                values[start + col] = v[row][col];
            }
        }
        return values;
    }

    public static double[] convert2double(Double[] v) {
        double[] values = new double[v.length];
        for (int idx = 0; idx < v.length; idx++) {
            values[idx] = v[idx];
        }
        return values;
    }
}
