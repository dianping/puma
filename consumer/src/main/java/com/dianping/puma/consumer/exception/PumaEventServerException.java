package com.dianping.puma.consumer.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class PumaEventServerException extends PumaException {

    public PumaEventServerException() {
    }

    public PumaEventServerException(String message) {
        super(message);
    }

    public PumaEventServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaEventServerException(Throwable cause) {
        super(cause);
    }

    public PumaEventServerException(String format, Object... arguments) {
        super(format, arguments);
    }
}
