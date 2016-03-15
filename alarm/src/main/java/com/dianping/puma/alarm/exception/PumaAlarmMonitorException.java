package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmMonitorException extends PumaAlarmException {

    public PumaAlarmMonitorException() {
    }

    public PumaAlarmMonitorException(String message) {
        super(message);
    }

    public PumaAlarmMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmMonitorException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmMonitorException(String format, Object... arguments) {
        super(format, arguments);
    }
}
