package com.dianping.puma.alarm.exception;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmJudgeException extends PumaAlarmException {

    public PumaAlarmJudgeException() {
    }

    public PumaAlarmJudgeException(String message) {
        super(message);
    }

    public PumaAlarmJudgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaAlarmJudgeException(Throwable cause) {
        super(cause);
    }

    public PumaAlarmJudgeException(String format, Object... arguments) {
        super(format, arguments);
    }
}
