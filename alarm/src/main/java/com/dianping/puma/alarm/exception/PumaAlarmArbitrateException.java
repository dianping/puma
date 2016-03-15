package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmArbitrateException extends PumaAlarmException {

    public PumaAlarmArbitrateException() {
    }

    public PumaAlarmArbitrateException(String message) {
        super(message);
    }

    public PumaAlarmArbitrateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmArbitrateException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmArbitrateException(String format, Object... arguments) {
        super(format, arguments);
    }
}
