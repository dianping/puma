package com.dianping.puma.common.exception;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaException extends RuntimeException {

    public PumaException() {
    }

    public PumaException(String message) {
        super(message);
    }

    public PumaException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaException(Throwable cause) {
        super(cause);
    }

    public PumaException(String format, Object... arguments) {
        this(parseMessage(format, arguments), parseThrowable(format, arguments));
    }

    private static String parseMessage(String format, Object... arguments) {
        int length = arguments.length;

        if (length == 0) {
            return format;
        } else if (!(arguments[length - 1] instanceof Throwable)) {
            return String.format(format, arguments);
        } else {
            Object[] stringArguments = ArrayUtils.remove(arguments, length - 1);
            return String.format(format, stringArguments);
        }
    }

    private static Throwable parseThrowable(String format, Object... arguments) {
        int length = arguments.length;

        if (length == 0) {
            return null;
        } else if (!(arguments[length - 1] instanceof Throwable)) {
            return null;
        } else {
            return (Throwable) arguments[length - 1];
        }
    }
}
