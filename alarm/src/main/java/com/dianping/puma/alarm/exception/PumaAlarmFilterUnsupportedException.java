package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmFilterUnsupportedException extends PumaAlarmFilterException {

    public PumaAlarmFilterUnsupportedException() {
    }

    public PumaAlarmFilterUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmFilterUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmFilterUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmFilterUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
