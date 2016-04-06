package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerHeartbeatManageException extends PumaAlarmException {

    public PumaAlarmServerHeartbeatManageException() {
    }

    public PumaAlarmServerHeartbeatManageException(String message) {
        super(message);
    }

    public PumaAlarmServerHeartbeatManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServerHeartbeatManageException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServerHeartbeatManageException(String format, Object... arguments) {
        super(format, arguments);
    }
}
