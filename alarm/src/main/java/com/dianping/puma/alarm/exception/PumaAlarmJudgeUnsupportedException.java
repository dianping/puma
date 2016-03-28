package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmJudgeUnsupportedException extends PumaAlarmException {

    public PumaAlarmJudgeUnsupportedException() {
    }

    public PumaAlarmJudgeUnsupportedException(String message) {
        super(message);
    }

    public PumaAlarmJudgeUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmJudgeUnsupportedException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmJudgeUnsupportedException(String format, Object... arguments) {
        super(format, arguments);
    }
}
