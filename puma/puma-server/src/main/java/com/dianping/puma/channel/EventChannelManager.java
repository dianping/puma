package com.dianping.puma.channel;

public interface EventChannelManager {
	public EventChannel getChannel(long seq);
}
