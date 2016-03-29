package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerHeartbeatException extends PumaAlarmException {

    public PumaAlarmServerHeartbeatException() {
    }

    public PumaAlarmServerHeartbeatException(String message) {
        super(message);
    }

    public PumaAlarmServerHeartbeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServerHeartbeatException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServerHeartbeatException(String format, Object... arguments) {
        super(format, arguments);
    }
}
