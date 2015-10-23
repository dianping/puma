package com.dianping.puma.storage.channel;

public class ChannelFactory {

	private ChannelFactory() {}

	public static ReadChannel newReadChannel(String database) {
		return new DefaultReadChannel(database);
	}

	public static WriteChannel newWriteChannel(String database) {
		return new DefaultWriteChannel(database);
	}
}
