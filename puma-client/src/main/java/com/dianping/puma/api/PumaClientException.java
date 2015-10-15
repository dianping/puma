package com.dianping.puma.api;

/**
 * PumaClientException
 */
public class PumaClientException extends Exception {
    public PumaClientException() {
    }

    public PumaClientException(String message) {
        super(message);
    }

    public PumaClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
