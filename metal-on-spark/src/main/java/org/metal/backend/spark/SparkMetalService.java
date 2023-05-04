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

import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.metal.core.props.IMetalProps;
import org.metal.dataset.spark.SchemaConvertorWithSpark;
import org.metal.exception.MetalServiceException;
import org.metal.service.BaseMetalService;
import org.metal.translator.Translator;

public class SparkMetalService<P extends IMetalProps> extends
    BaseMetalService<Dataset<Row>, SparkSession, P> {

  protected SparkMetalService(Translator<Dataset<Row>, SparkSession> translator) {
    super(translator);
  }

  @Override
  public Schema schema(String id) throws MetalServiceException {
    try {
      StructType schema = df(id).schema();
      String timeZoneId = translator().platform().sessionState().conf().sessionLocalTimeZone();
      Schema target = new SchemaConvertorWithSpark().from(schema, timeZoneId);
      return target;
    } catch (Exception e) {
      throw new MetalServiceException(e);
    }
  }

  public static <P extends IMetalProps> SparkMetalService<P> of(SparkTranslator translator) {
    return new SparkMetalService<P>(translator);
  }
}
