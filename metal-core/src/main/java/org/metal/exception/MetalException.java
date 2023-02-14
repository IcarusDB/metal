package org.metal.exception;

public class MetalException extends  RuntimeException{

  public MetalException() {
  }

  public MetalException(String message) {
    super(message);
  }

  public MetalException(String message, String metal) {
    super("Metal[" + metal + "]" + message);
  }

  public MetalException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalException(String message, Throwable cause, String metal) {
    super("Metal[" + metal + "]" + message, cause);
  }

  public MetalException(Throwable cause) {
    super(cause);
  }

  public MetalException(Throwable cause, String metal) {
    super("Metal[" + metal + "]", cause);
  }

  public MetalException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalException(String message, Throwable cause, String metal, boolean enableSuppression,
      boolean writableStackTrace) {
    super("Metal[" + metal + "]" + message, cause, enableSuppression, writableStackTrace);
  }
}
