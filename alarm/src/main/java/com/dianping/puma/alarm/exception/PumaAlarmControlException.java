package com.dianping.puma.alarm.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmControlException extends PumaException {

    public PumaAlarmControlException() {
    }

    public PumaAlarmControlException(String message) {
        super(message);
    }

    public PumaAlarmControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmControlException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmControlException(String format, Object... arguments) {
        super(format, arguments);
    }
}
