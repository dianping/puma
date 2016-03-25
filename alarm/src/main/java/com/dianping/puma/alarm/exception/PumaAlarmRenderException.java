package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmRenderException extends PumaAlarmException {

    public PumaAlarmRenderException() {
    }

    public PumaAlarmRenderException(String message) {
        super(message);
    }

    public PumaAlarmRenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmRenderException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmRenderException(String format, Object... arguments) {
        super(format, arguments);
    }
}
