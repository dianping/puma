package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmProcessUnsupportedException extends PumaAlarmProcessException {

    public PumaAlarmProcessUnsupportedException() {
    }

    public PumaAlarmProcessUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmProcessUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmProcessUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmProcessUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
