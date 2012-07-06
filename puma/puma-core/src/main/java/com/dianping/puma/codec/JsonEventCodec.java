package com.dianping.puma.codec;

import com.dianping.puma.core.event.ChangedEvent;

public class JsonEventCodec implements EventCodec {
	@Override
   public byte[] encode(ChangedEvent event) {
	   return null;
   }

	@Override
   public ChangedEvent decode(byte[] data) {
	   return null;
   }
}
