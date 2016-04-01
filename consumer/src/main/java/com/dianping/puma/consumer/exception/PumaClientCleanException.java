package com.dianping.puma.consumer.exception;

/**
 * Created by xiaotian.li on 16/4/1.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientCleanException extends PumaEventServerException {

    public PumaClientCleanException() {
    }

    public PumaClientCleanException(String message) {
        super(message);
    }

    public PumaClientCleanException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaClientCleanException(Throwable cause) {
        super(cause);
    }

    public PumaClientCleanException(String format, Object... arguments) {
        super(format, arguments);
    }
}
