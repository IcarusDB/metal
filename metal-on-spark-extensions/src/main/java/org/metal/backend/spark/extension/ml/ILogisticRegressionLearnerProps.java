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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;
import org.metal.core.props.IMSinkProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableILogisticRegressionLearnerProps.class)
@JsonSerialize(as = ImmutableILogisticRegressionLearnerProps.class)
public interface ILogisticRegressionLearnerProps extends IMSinkProps, ILearnerProps {

  public Optional<Integer> aggregationDepth();

  public Optional<Double> elasticNetParam();

  public Optional<String> family();

  public Optional<Boolean> fitIntercept();

  public Optional<String> initialModel();

  public Optional<Double[][]> lowerBoundsOnCoefficients();

  public Optional<Double[]> lowerBoundsOnIntercepts();

  public Optional<Double> maxBlockSizeInMB();

  public Optional<Integer> maxIter();

  public Optional<Double> regParam();

  public Optional<Boolean> standardization();

  public Optional<Double[]> thresholds();

  public Optional<Double> threshold();

  public Optional<Double> tol();

  public Optional<Double[][]> upperBoundsOnCoefficients();

  public Optional<Double[]> upperBoundsOnIntercepts();

  public Optional<String> weightCol();

  public String savePath();
}
