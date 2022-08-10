package org.metal.core.exception;

public class MetalAnalysedException extends RuntimeException{
    public MetalAnalysedException() {
        super();
    }

    public MetalAnalysedException(String message) {
        super(message);
    }

    public MetalAnalysedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetalAnalysedException(Throwable cause) {
        super(cause);
    }

    public MetalAnalysedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
