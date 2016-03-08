package com.dianping.puma.common.intercept.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaInterceptException extends PumaException {

    public PumaInterceptException() {
    }

    public PumaInterceptException(String message) {
        super(message);
    }

    public PumaInterceptException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaInterceptException(Throwable cause) {
        super(cause);
    }

    public PumaInterceptException(String format, Object... arguments) {
        super(format, arguments);
    }
}
