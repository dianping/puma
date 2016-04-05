package com.dianping.puma.common.exception;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PumaServiceException extends PumaException {

    public PumaServiceException() {
    }

    public PumaServiceException(String message) {
        super(message);
    }

    public PumaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaServiceException(Throwable cause) {
        super(cause);
    }

    public PumaServiceException(String format, Object... arguments) {
        super(format, arguments);
    }
}
