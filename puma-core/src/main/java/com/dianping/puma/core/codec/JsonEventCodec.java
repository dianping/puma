package com.dianping.puma.core.codec;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.ServerErrorEvent;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class JsonEventCodec implements EventCodec {
    private ObjectMapper om;

    public JsonEventCodec() {
        om = new ObjectMapper();
        om.enableDefaultTyping();
        om.getSerializerProvider().setNullKeySerializer(new MapNullKeySerializer());
    }

    private static class MapNullKeySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused)
                throws IOException, JsonProcessingException {
            jsonGenerator.writeFieldName("[NullKey]");
        }
    }

    @Override
    public byte[] encode(Event event) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] data = om.writeValueAsBytes(event);

        if (event instanceof DdlEvent) {
            out.write(DDL_EVENT);
        } else if (event instanceof RowChangedEvent) {
            out.write(DML_EVENT);
        } else {
            out.write(SERVER_ERROR_EVENT);
        }

        out.write(data);

        return out.toByteArray();
    }

    @Override
    public byte[] encodeList(List<Event> event) throws IOException {
        return new byte[0];
    }

    @Override
    public Event decode(byte[] data) throws IOException {
        int type = data[0];
        if (type == DDL_EVENT) {
            return om.readValue(data, 1, data.length - 1, DdlEvent.class);
        } else if (type == DML_EVENT) {
            return om.readValue(data, 1, data.length - 1, RowChangedEvent.class);
        } else {
            return om.readValue(data, 1, data.length - 1, ServerErrorEvent.class);
        }
    }

    @Override
    public List<Event> decodeList(byte[] data) throws IOException {
        return null;
    }
}
