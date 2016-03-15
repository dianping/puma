package com.dianping.puma.server.exception;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class PumaEventRequestDecoderException extends PumaException {

    public PumaEventRequestDecoderException() {
    }

    public PumaEventRequestDecoderException(String message) {
        super(message);
    }

    public PumaEventRequestDecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public PumaEventRequestDecoderException(Throwable cause) {
        super(cause);
    }

    public PumaEventRequestDecoderException(String format, Object... arguments) {
        super(format, arguments);
    }
}
