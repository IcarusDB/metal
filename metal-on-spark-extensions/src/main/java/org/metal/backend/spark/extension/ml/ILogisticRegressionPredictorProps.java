package org.metal.backend.spark.extension.ml;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMMapperProps;

import java.util.Optional;

@Value.Immutable
@JsonDeserialize(as = ImmutableILogisticRegressionPredictorProps.class)
@JsonSerialize(as = ImmutableILogisticRegressionPredictorProps.class)
public interface ILogisticRegressionPredictorProps extends IMMapperProps, IPredictorProps {
    public Optional<Double[]> thresholds();
    public Optional<Double> threshold();
    public String modelPath();
}