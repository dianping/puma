package com.dianping.puma.api.connector.exception;

/**
 * Dozer @ 7/6/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaClientException extends Exception{
    public PumaClientException() {
    }

    public PumaClientException(String message) {
        super(message);
    }

    public PumaClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
