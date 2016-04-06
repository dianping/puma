package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerLeaderManageException extends PumaAlarmException {

    public PumaAlarmServerLeaderManageException() {
    }

    public PumaAlarmServerLeaderManageException(String message) {
        super(message);
    }

    public PumaAlarmServerLeaderManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServerLeaderManageException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServerLeaderManageException(String format, Object... arguments) {
        super(format, arguments);
    }
}
