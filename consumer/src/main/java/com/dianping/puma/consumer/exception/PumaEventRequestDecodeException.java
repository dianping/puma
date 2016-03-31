package com.dianping.puma.consumer.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class PumaEventRequestDecodeException extends PumaException {

    public PumaEventRequestDecodeException() {
    }

    public PumaEventRequestDecodeException(String message) {
        super(message);
    }

    public PumaEventRequestDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaEventRequestDecodeException(Throwable cause) {
        super(cause);
    }

    public PumaEventRequestDecodeException(String format, Object... arguments) {
        super(format, arguments);
    }
}
