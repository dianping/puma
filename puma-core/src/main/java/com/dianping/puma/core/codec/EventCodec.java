package com.dianping.puma.core.codec;

import java.io.IOException;

import com.dianping.puma.core.event.Event;

public interface EventCodec {
    public static final int DDL_EVENT   = 0;
    public static final int DML_EVENT   = 1;

    public byte[] encode(Event event) throws IOException;

    public Event decode(byte[] data) throws IOException;

}