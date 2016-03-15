package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmProcessException extends PumaAlarmException {

    public PumaAlarmProcessException() {
    }

    public PumaAlarmProcessException(String message) {
        super(message);
    }

    public PumaAlarmProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmProcessException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmProcessException(String format, Object... arguments) {
        super(format, arguments);
    }
}
