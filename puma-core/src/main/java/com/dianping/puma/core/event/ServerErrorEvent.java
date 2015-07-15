package com.dianping.puma.core.event;

import com.dianping.puma.core.model.BinlogInfo;

public class ServerErrorEvent extends Event {

    private static final long serialVersionUID = 133827935042159681L;

    private final EventType eventType = EventType.ERROR;

    private String msg;

    private Throwable cause;

    public ServerErrorEvent() {

    }

    public ServerErrorEvent(String msg) {
        this.msg = msg;
    }

    public ServerErrorEvent(String msg, Throwable cause) {
        this.msg = msg;
        this.cause = cause;
    }

    public String getMsg() {
        return msg;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public BinlogInfo getBinlogInfo() {
        return null;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }
}
