package com.dianping.puma.syncserver.bo;

public class DumpException extends RuntimeException {
    private static final long serialVersionUID = -7383812288733645333L;

    public DumpException(String message, Throwable cause) {
        super(message, cause);
    }

    public DumpException(String message) {
        super(message);
    }

}
