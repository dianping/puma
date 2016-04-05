package com.dianping.puma.alarm.deploy.exception;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerHAManageException extends PumaAlarmServerDeployException {

    public PumaAlarmServerHAManageException() {
    }

    public PumaAlarmServerHAManageException(String message) {
        super(message);
    }

    public PumaAlarmServerHAManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServerHAManageException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServerHAManageException(String format, Object... arguments) {
        super(format, arguments);
    }
}
