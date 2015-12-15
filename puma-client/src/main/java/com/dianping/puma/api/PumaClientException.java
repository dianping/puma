package com.dianping.puma.api;

/**
 * PumaClientException
 */
public class PumaClientException extends Exception {

    private static final long serialVersionUID = 5575514632552935756L;

    public PumaClientException() {
    }

    public PumaClientException(String message) {
        super(message);
    }

    public PumaClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
