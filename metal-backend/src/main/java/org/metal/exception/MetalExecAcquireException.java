package org.metal.exception;

public class MetalExecAcquireException extends RuntimeException{

  public MetalExecAcquireException() {
    super();
  }

  public MetalExecAcquireException(String message) {
    super(message);
  }

  public MetalExecAcquireException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalExecAcquireException(Throwable cause) {
    super(cause);
  }

  public MetalExecAcquireException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
