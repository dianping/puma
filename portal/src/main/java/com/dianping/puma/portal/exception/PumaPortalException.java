package com.dianping.puma.portal.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public class PumaPortalException extends PumaException {

    public PumaPortalException() {
    }

    public PumaPortalException(String message) {
        super(message);
    }

    public PumaPortalException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaPortalException(Throwable cause) {
        super(cause);
    }

    public PumaPortalException(String format, Object... arguments) {
        super(format, arguments);
    }
}
