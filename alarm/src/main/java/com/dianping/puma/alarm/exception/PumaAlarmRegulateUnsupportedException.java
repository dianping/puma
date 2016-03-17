package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmRegulateUnsupportedException extends PumaAlarmRegulateException {

    public PumaAlarmRegulateUnsupportedException() {
    }

    public PumaAlarmRegulateUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmRegulateUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmRegulateUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmRegulateUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
