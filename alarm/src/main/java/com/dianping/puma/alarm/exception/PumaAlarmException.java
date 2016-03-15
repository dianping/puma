package com.dianping.puma.alarm.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmException extends PumaException {

    public PumaAlarmException() {
    }

    public PumaAlarmException(String message) {
        super(message);
    }

    public PumaAlarmException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmException(String format, Object... arguments) {
        super(format, arguments);
    }
}
