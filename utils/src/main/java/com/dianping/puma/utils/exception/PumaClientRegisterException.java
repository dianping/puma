package com.dianping.puma.utils.exception;

import com.dianping.puma.core.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientRegisterException extends PumaException {

    public PumaClientRegisterException() {
    }

    public PumaClientRegisterException(String message) {
        super(message);
    }

    public PumaClientRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaClientRegisterException(Throwable cause) {
        super(cause);
    }

    public PumaClientRegisterException(String format, Object... arguments) {
        super(format, arguments);
    }
}
