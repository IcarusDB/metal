package org.metal.backend.spark.extension.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.linalg.Matrices;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.backend.spark.SparkMSink;
import org.metal.core.IMExecutor;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalTranslateException;

import java.io.IOException;

public class LogisticRegressionLearner extends SparkMSink<ILogisticRegressionLearnerProps> {
    @JsonCreator
    public LogisticRegressionLearner(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("props") ILogisticRegressionLearnerProps props) {
        super(id, name, props);
    }

    @Override
    public IMExecutor sink(SparkSession platform, Dataset<Row> data) throws MetalTranslateException {
        LogisticRegression learner = new LogisticRegression();
        ILogisticRegressionLearnerProps props = this.props();

        props.aggregationDepth().ifPresent(v -> learner.setAggregationDepth(v));
        props.elasticNetParam().ifPresent(v -> learner.setElasticNetParam(v));
        props.family().ifPresent(v -> learner.setFamily(v));
        props.fitIntercept().ifPresent(v -> learner.setFitIntercept(v));

        props.initialModel().ifPresent((String v) -> {
            LogisticRegressionModel model = LogisticRegressionModel.load(v);
            learner.setInitialModel(model);
        });

        props.lowerBoundsOnCoefficients().ifPresent((Double[][] v) -> {
            int numRows = v.length;
            int numCols = v[0].length;

            double[] values = Convertor.flat2DArray(v);
            learner.setLowerBoundsOnCoefficients(Matrices.dense(numRows, numCols, values));
        });

        props.lowerBoundsOnIntercepts().ifPresent((Double[] v) -> {
            double[] values = Convertor.convert2double(v);
            learner.setLowerBoundsOnIntercepts(Vectors.dense(values));
        });

        props.maxBlockSizeInMB().ifPresent(v -> learner.setMaxBlockSizeInMB(v));
        props.maxIter().ifPresent(v -> learner.setMaxIter(v));
        props.regParam().ifPresent(v -> learner.setRegParam(v));
        props.standardization().ifPresent(v -> learner.setStandardization(v));
        props.thresholds().ifPresent(v -> learner.setThresholds(Convertor.convert2double(v)));
        props.threshold().ifPresent(v -> learner.setThreshold(v));
        props.tol().ifPresent(v -> learner.setTol(v));
        learner.setFeaturesCol(props.featuresCol());
        learner.setLabelCol(props.labelCol());

        props.upperBoundsOnCoefficients().ifPresent((Double[][] v) -> {
            int numRows = v.length;
            int numCols = v[0].length;

            double[] values = Convertor.flat2DArray(v);
            learner.setUpperBoundsOnCoefficients(Matrices.dense(numRows, numCols, values));
        });

        props.upperBoundsOnIntercepts().ifPresent((Double[] v) -> {
            double[] values = Convertor.convert2double(v);
            learner.setUpperBoundsOnIntercepts(Vectors.dense(values));
        });

        props.weightCol().ifPresent(v -> learner.setWeightCol(v));

        LogisticRegressionModel model = learner.fit(data);

        return () -> {
            try {
                model.write().overwrite().save(props.savePath());
            } catch (IOException e) {
                throw new MetalExecuteException(e);
            }
        };
    }

}
