package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmRenderUnsupportedException extends PumaAlarmRenderException {

    public PumaAlarmRenderUnsupportedException() {
    }

    public PumaAlarmRenderUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmRenderUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmRenderUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmRenderUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
