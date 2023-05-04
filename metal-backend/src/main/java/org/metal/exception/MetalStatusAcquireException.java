package org.metal.exception;

public class MetalStatusAcquireException extends RuntimeException {

  public MetalStatusAcquireException() {
    super();
  }

  public MetalStatusAcquireException(String message) {
    super(message);
  }

  public MetalStatusAcquireException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalStatusAcquireException(Throwable cause) {
    super(cause);
  }

  public MetalStatusAcquireException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
