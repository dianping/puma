package com.dianping.puma.server.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientManageException extends PumaException {

    public PumaClientManageException() {
    }

    public PumaClientManageException(String message) {
        super(message);
    }

    public PumaClientManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaClientManageException(Throwable cause) {
        super(cause);
    }

    public PumaClientManageException(String format, Object... arguments) {
        super(format, arguments);
    }
}
