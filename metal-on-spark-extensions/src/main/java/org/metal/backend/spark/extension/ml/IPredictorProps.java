package org.metal.backend.spark.extension.ml;

import java.util.Optional;

public interface IPredictorProps {

  public String featuresCol();

  public Optional<String> predictionCol();

  public Optional<String> probabilityCol();

  public Optional<String> rawPredictionCol();
}
