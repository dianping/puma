package com.dianping.puma.portal.exception;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public class PumaDeviceException extends PumaWebException {

    public PumaDeviceException() {
    }

    public PumaDeviceException(String message) {
        super(message);
    }

    public PumaDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaDeviceException(Throwable cause) {
        super(cause);
    }

    public PumaDeviceException(String format, Object... arguments) {
        super(format, arguments);
    }
}
