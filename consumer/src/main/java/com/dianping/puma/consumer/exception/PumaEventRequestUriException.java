package com.dianping.puma.consumer.exception;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class PumaEventRequestUriException extends PumaEventRequestDecodeException {

    public PumaEventRequestUriException() {
    }

    public PumaEventRequestUriException(String message) {
        super(message);
    }

    public PumaEventRequestUriException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaEventRequestUriException(Throwable cause) {
        super(cause);
    }

    public PumaEventRequestUriException(String format, Object... arguments) {
        super(format, arguments);
    }
}
