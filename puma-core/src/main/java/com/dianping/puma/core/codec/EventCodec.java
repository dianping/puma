package com.dianping.puma.core.codec;

import com.dianping.puma.core.event.Event;

import java.io.IOException;
import java.util.List;

public interface EventCodec {
    int DDL_EVENT = 0;
    int DML_EVENT = 1;

    byte[] encode(Event event) throws IOException;

    byte[] encodeList(List<Event> event) throws IOException;

    Event decode(byte[] data) throws IOException;

    List<Event> decodeList(byte[] data) throws IOException;
}