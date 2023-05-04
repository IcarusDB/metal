package org.metal.exception;

public class MetalSpecParseException extends MetalException {

  public MetalSpecParseException() {
  }

  public MetalSpecParseException(String message) {
    super(message);
  }

  public MetalSpecParseException(String message, String metal) {
    super(message, metal);
  }

  public MetalSpecParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalSpecParseException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalSpecParseException(Throwable cause) {
    super(cause);
  }

  public MetalSpecParseException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalSpecParseException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalSpecParseException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

