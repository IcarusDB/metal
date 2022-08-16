package org.metal.exception;

public class MetalTranslateException extends RuntimeException{
    public MetalTranslateException() {
        super();
    }

    public MetalTranslateException(String message) {
        super(message);
    }

    public MetalTranslateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetalTranslateException(Throwable cause) {
        super(cause);
    }

    public MetalTranslateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
