package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmArbitrateUnsupportedException extends PumaAlarmException {

    public PumaAlarmArbitrateUnsupportedException() {
    }

    public PumaAlarmArbitrateUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmArbitrateUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmArbitrateUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmArbitrateUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
