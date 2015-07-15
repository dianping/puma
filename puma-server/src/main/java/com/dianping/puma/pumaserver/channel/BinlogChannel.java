package com.dianping.puma.pumaserver.channel;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;

import java.util.concurrent.TimeUnit;

public interface BinlogChannel {

	void init(String targetName, long dbServerId, long sc, BinlogInfo binlogInfo, long timestamp)
			throws BinlogChannelException;

	void destroy() throws BinlogChannelException;

	Event next() throws BinlogChannelException;

	Event next(long timeout, TimeUnit timeUnit) throws BinlogChannelException;
}
