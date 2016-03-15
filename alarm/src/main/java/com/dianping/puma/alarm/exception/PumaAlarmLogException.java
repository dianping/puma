package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmLogException extends PumaAlarmException {

    public PumaAlarmLogException() {
    }

    public PumaAlarmLogException(String message) {
        super(message);
    }

    public PumaAlarmLogException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmLogException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmLogException(String format, Object... arguments) {
        super(format, arguments);
    }
}
