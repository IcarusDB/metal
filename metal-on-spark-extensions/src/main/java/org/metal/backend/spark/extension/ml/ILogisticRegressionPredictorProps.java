package org.metal.backend.spark.extension.ml;

import org.immutables.value.Value;
import org.metal.core.props.IMMapperProps;

import java.util.Optional;

@Value.Immutable
public interface ILogisticRegressionPredictorProps extends IMMapperProps, IPredictorProps {
    public Optional<Double[]> thresholds();
    public Optional<Double> threshold();
    public String modelPath();
}
