package org.metal.core.exception;

public class MetalForgeException extends RuntimeException{
    public MetalForgeException() {
        super();
    }

    public MetalForgeException(String message) {
        super(message);
    }

    public MetalForgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetalForgeException(Throwable cause) {
        super(cause);
    }

    public MetalForgeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
