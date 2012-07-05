package com.dianping.puma.storage.codec;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventCodec {
	public byte[] encode(ChangedEvent event);
	
	public ChangedEvent decode(byte[] data);
}
