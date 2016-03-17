package com.dianping.puma.alarm.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmRegulateException extends PumaException {

    public PumaAlarmRegulateException() {
    }

    public PumaAlarmRegulateException(String message) {
        super(message);
    }

    public PumaAlarmRegulateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmRegulateException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmRegulateException(String format, Object... arguments) {
        super(format, arguments);
    }
}
