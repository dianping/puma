package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmNotifyException extends PumaAlarmException {

    public PumaAlarmNotifyException() {
    }

    public PumaAlarmNotifyException(String message) {
        super(message);
    }

    public PumaAlarmNotifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmNotifyException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmNotifyException(String format, Object... arguments) {
        super(format, arguments);
    }
}
