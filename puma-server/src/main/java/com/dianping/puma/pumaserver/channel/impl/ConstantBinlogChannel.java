package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.exception.BinlogChannelException;

public class ConstantBinlogChannel implements BinlogChannel {

	private final ChangedEvent constant;

	public ConstantBinlogChannel() {
		constant = new RowChangedEvent();
		constant.setDatabase("puma-test-database");
		constant.setTable("puma-test-table");
	}

	@Override
	public ChangedEvent next() throws BinlogChannelException {
		return null;
	}

	@Override
	public ChangedEvent read(BinlogInfo binlogInfo) throws BinlogChannelException {
		return null;
	}
}
