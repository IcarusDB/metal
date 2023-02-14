package org.metal.exception;

public class MetalDraftException extends MetalException{

  public MetalDraftException() {
  }

  public MetalDraftException(String message) {
    super(message);
  }

  public MetalDraftException(String message, String metal) {
    super(message, metal);
  }

  public MetalDraftException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalDraftException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalDraftException(Throwable cause) {
    super(cause);
  }

  public MetalDraftException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalDraftException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalDraftException(String message, Throwable cause, String metal,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

