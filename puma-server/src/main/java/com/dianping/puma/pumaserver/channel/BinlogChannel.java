package com.dianping.puma.pumaserver.channel;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.exception.BinlogChannelException;

import java.util.concurrent.TimeUnit;

public interface BinlogChannel {

	void locate(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp)
			throws BinlogChannelException;

	void destroy() throws BinlogChannelException;

	ChangedEvent next() throws BinlogChannelException;

	ChangedEvent next(long timeout, TimeUnit timeUnit) throws BinlogChannelException;
}
