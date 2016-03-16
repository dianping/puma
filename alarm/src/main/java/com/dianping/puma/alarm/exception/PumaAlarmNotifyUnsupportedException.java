package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmNotifyUnsupportedException extends PumaAlarmException {

    public PumaAlarmNotifyUnsupportedException() {
    }

    public PumaAlarmNotifyUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmNotifyUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmNotifyUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmNotifyUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
