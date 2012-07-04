package com.dianping.puma.channel;

public enum DefaultEventChannelManager implements EventChannelManager {
	INSTANCE;

	@Override
	public EventChannel getChannel(long seq) {
		return null;
	}
}
