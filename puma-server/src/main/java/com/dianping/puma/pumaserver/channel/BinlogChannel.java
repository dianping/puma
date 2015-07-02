package com.dianping.puma.pumaserver.channel;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.exception.BinlogChannelException;

public interface BinlogChannel {

	void locate(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp)
			throws BinlogChannelException;

	ChangedEvent next() throws BinlogChannelException;

	ChangedEvent read(BinlogInfo binlogInfo) throws BinlogChannelException;
}
