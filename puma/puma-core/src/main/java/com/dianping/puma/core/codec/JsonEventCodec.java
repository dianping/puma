package com.dianping.puma.core.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class JsonEventCodec implements EventCodec {
	@Override
	public byte[] encode(ChangedEvent event) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ObjectMapper om = new ObjectMapper();
		byte[] data = om.writeValueAsBytes(event);

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
		ObjectMapper om = new ObjectMapper();
		int type = data[0];
		if (type == DDL_EVENT) {
			return om.readValue(data, 1, data.length - 1, DdlEvent.class);
		} else {
			return om.readValue(data, 1, data.length - 1, RowChangedEvent.class);
		}
	}

}
