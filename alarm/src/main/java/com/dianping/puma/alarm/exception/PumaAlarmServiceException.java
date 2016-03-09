package com.dianping.puma.alarm.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServiceException extends PumaException {

    public PumaAlarmServiceException() {
    }

    public PumaAlarmServiceException(String message) {
        super(message);
    }

    public PumaAlarmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmServiceException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmServiceException(String format, Object... arguments) {
        super(format, arguments);
    }
}
