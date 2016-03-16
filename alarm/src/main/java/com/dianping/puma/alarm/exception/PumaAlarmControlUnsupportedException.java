package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmControlUnsupportedException extends PumaAlarmControlException {

    public PumaAlarmControlUnsupportedException() {
    }

    public PumaAlarmControlUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmControlUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmControlUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmControlUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
