package org.metal.exception;

public class MetalExecuteException extends MetalException{

  public MetalExecuteException() {
  }

  public MetalExecuteException(String message) {
    super(message);
  }

  public MetalExecuteException(String message, String metal) {
    super(message, metal);
  }

  public MetalExecuteException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalExecuteException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalExecuteException(Throwable cause) {
    super(cause);
  }

  public MetalExecuteException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalExecuteException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalExecuteException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

