package com.dianping.puma.consumer.exception;

/**
 * Created by xiaotian.li on 16/3/30.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientMetaManageException extends PumaEventServerException {

    public PumaClientMetaManageException() {
    }

    public PumaClientMetaManageException(String message) {
        super(message);
    }

    public PumaClientMetaManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaClientMetaManageException(Throwable cause) {
        super(cause);
    }

    public PumaClientMetaManageException(String format, Object... arguments) {
        super(format, arguments);
    }
}
