package com.dianping.puma.portal.exception;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaStatusProcessException extends PumaPortalException {

    public PumaStatusProcessException() {
    }

    public PumaStatusProcessException(String message) {
        super(message);
    }

    public PumaStatusProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaStatusProcessException(Throwable cause) {
        super(cause);
    }

    public PumaStatusProcessException(String format, Object... arguments) {
        super(format, arguments);
    }
}
