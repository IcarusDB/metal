package org.metal.backend.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.metal.core.MSource;
import org.metal.core.props.IMSourceProps;

public abstract class SparkMSource<P extends IMSourceProps> extends
    MSource<Dataset<Row>, SparkSession, P> {

  public SparkMSource(String id, String name, P props) {
    super(id, name, props);
  }
}
