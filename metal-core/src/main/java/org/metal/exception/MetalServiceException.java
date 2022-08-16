package org.metal.exception;

public class MetalServiceException extends RuntimeException{
    public MetalServiceException() {
        super();
    }

    public MetalServiceException(String message) {
        super(message);
    }

    public MetalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetalServiceException(Throwable cause) {
        super(cause);
    }

    public MetalServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
