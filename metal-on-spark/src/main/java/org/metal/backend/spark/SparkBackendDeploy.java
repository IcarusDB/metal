package org.metal.backend.spark;

import org.apache.spark.deploy.SparkSubmit$;
import org.metal.backend.IBackendDeploy;


public class SparkBackendDeploy implements IBackendDeploy {

  @Override
  public void deploy(String[] args) {
    SparkSubmit$.MODULE$.main(args);
  }
}
