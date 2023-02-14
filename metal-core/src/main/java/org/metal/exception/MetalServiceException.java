package org.metal.exception;

public class MetalServiceException extends MetalException{

  public MetalServiceException() {
  }

  public MetalServiceException(String message) {
    super(message);
  }

  public MetalServiceException(String message, String metal) {
    super(message, metal);
  }

  public MetalServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalServiceException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalServiceException(Throwable cause) {
    super(cause);
  }

  public MetalServiceException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalServiceException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalServiceException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

