package org.metal.exception;

public class MetalSpecParseException extends RuntimeException{

  public MetalSpecParseException() {
    super();
  }

  public MetalSpecParseException(String message) {
    super(message);
  }

  public MetalSpecParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalSpecParseException(Throwable cause) {
    super(cause);
  }

  public MetalSpecParseException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
