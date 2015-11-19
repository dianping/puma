package com.dianping.puma.pumaserver.exception.client;

import com.dianping.puma.pumaserver.exception.PumaServerException;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientNotRegisterException extends PumaServerException {
    public ClientNotRegisterException(String msg) {
        super(msg);
    }

    public ClientNotRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
