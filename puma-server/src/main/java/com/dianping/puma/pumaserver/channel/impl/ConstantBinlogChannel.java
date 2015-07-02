package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.exception.BinlogChannelException;

import java.util.concurrent.TimeUnit;

public class ConstantBinlogChannel implements BinlogChannel {

	private final ChangedEvent constant;

	public ConstantBinlogChannel() {
		constant = new RowChangedEvent();
		constant.setDatabase("puma-test-database");
		constant.setTable("puma-test-table");
	}

	@Override
	public void locate(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp)
			throws BinlogChannelException {

	}

	@Override
	public ChangedEvent next() throws BinlogChannelException {
		ChangedEvent event = new RowChangedEvent();
		event.setDatabase(constant.getDatabase());
		event.setTable(constant.getTable());
		return event;
	}

	@Override
	public ChangedEvent next(long timeout, TimeUnit timeUnit) throws BinlogChannelException {
		return null;
	}
}
