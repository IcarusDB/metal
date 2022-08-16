package org.metal.exception;

public class MetalExecuteException extends RuntimeException{
    public MetalExecuteException() {
        super();
    }

    public MetalExecuteException(String message) {
        super(message);
    }

    public MetalExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetalExecuteException(Throwable cause) {
        super(cause);
    }

    public MetalExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
