package com.dianping.puma.pumaserver.channel;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.exception.BinlogChannelException;

public interface BinlogChannel {

	void locate(BinlogInfo binlogInfo) throws BinlogChannelException;

	ChangedEvent next() throws BinlogChannelException;

	ChangedEvent read(BinlogInfo binlogInfo) throws BinlogChannelException;
}
