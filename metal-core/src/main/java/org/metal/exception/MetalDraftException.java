package org.metal.exception;

public class MetalDraftException extends RuntimeException{

  public MetalDraftException() {
    super();
  }

  public MetalDraftException(String message) {
    super(message);
  }

  public MetalDraftException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalDraftException(Throwable cause) {
    super(cause);
  }

  public MetalDraftException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
