package com.dianping.puma.alarm.deploy.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerDeployException extends PumaException {

    public PumaAlarmServerDeployException() {
    }

    public PumaAlarmServerDeployException(String message) {
        super(message);
    }

    public PumaAlarmServerDeployException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServerDeployException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServerDeployException(String format, Object... arguments) {
        super(format, arguments);
    }
}
