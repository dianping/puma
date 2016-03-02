package com.dianping.puma.pumaserver.client.exception;

import com.dianping.puma.core.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/2.
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
