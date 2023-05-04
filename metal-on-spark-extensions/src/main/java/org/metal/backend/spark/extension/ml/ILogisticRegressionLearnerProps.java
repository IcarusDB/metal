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
