package com.dianping.puma.server.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class PumaEventRequestUriException extends PumaException {

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
