package org.metal.exception;

public class MetalAnalysedException extends MetalException {

  public MetalAnalysedException() {
  }

  public MetalAnalysedException(String message) {
    super(message);
  }

  public MetalAnalysedException(String message, String metal) {
    super(message, metal);
  }

  public MetalAnalysedException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalAnalysedException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalAnalysedException(Throwable cause) {
    super(cause);
  }

  public MetalAnalysedException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalAnalysedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalAnalysedException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}
