package com.dianping.puma.core.netty.exception;

public class PumaClientException extends Exception {
    private final boolean needReconnection;
    private final boolean needSwitch;
    private final String message;

    public PumaClientException(boolean needReconnection, boolean needSwitch, String message) {
        this.needReconnection = needReconnection;
        this.needSwitch = needSwitch;
        this.message = message;
    }

    public boolean isNeedReconnection() {
        return needReconnection;
    }

    public boolean isNeedSwitch() {
        return needSwitch;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
