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

package org.metal.backend.spark;

import org.metal.core.MFusion;
import org.metal.core.props.IMFusionProps;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public abstract class SparkMFusion<P extends IMFusionProps>
        extends MFusion<Dataset<Row>, SparkSession, P> {

    public SparkMFusion(String id, String name, P props) {
        super(id, name, props);
    }
}
