package com.dianping.puma.core.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class JsonEventCodec implements EventCodec {

    @Override
    public byte[] encode(ChangedEvent event) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] data = JSON.toJSONBytes(event, SerializerFeature.WriteMapNullValue);

        if (event instanceof DdlEvent) {
            out.write(DDL_EVENT);
        } else {
            out.write(DML_EVENT);
        }

        out.write(data);

        return out.toByteArray();
    }

    @Override
    public ChangedEvent decode(byte[] data) throws IOException {
        int type = data[0];
        byte[] newData = new byte[data.length - 1];
        System.arraycopy(data, 1, newData, 0, data.length - 1);
        if (type == DDL_EVENT) {
            return JSON.parseObject(newData, DdlEvent.class);
        } else {
            return JSON.parseObject(newData, RowChangedEvent.class);
        }
    }

}
