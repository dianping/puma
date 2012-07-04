package com.dianping.puma.channel;

public class RawEvent {
	private byte[] data;
	
	public RawEvent(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
   	return data;
   }
}
