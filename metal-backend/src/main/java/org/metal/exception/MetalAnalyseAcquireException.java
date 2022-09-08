package org.metal.exception;

public class MetalAnalyseAcquireException extends RuntimeException{

  public MetalAnalyseAcquireException() {
    super();
  }

  public MetalAnalyseAcquireException(String message) {
    super(message);
  }

  public MetalAnalyseAcquireException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalAnalyseAcquireException(Throwable cause) {
    super(cause);
  }

  public MetalAnalyseAcquireException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
