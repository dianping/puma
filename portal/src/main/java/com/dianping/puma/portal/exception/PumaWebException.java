package com.dianping.puma.portal.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public class PumaWebException extends PumaException {

    public PumaWebException() {
    }

    public PumaWebException(String message) {
        super(message);
    }

    public PumaWebException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaWebException(Throwable cause) {
        super(cause);
    }

    public PumaWebException(String format, Object... arguments) {
        super(format, arguments);
    }
}
