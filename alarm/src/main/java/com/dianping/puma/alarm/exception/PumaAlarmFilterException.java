package com.dianping.puma.alarm.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmFilterException extends PumaException {

    public PumaAlarmFilterException() {
    }

    public PumaAlarmFilterException(String message) {
        super(message);
    }

    public PumaAlarmFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmFilterException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmFilterException(String format, Object... arguments) {
        super(format, arguments);
    }
}
