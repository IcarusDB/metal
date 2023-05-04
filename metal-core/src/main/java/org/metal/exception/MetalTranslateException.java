package org.metal.exception;

public class MetalTranslateException extends MetalException {

  public MetalTranslateException() {
  }

  public MetalTranslateException(String message) {
    super(message);
  }

  public MetalTranslateException(String message, String metal) {
    super(message, metal);
  }

  public MetalTranslateException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalTranslateException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalTranslateException(Throwable cause) {
    super(cause);
  }

  public MetalTranslateException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalTranslateException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalTranslateException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

