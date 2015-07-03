package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;

import java.util.concurrent.TimeUnit;

public class ConstantBinlogChannel implements BinlogChannel {

	private final ChangedEvent constant;

	private final long costTime = 1;
	private final TimeUnit costTimeUnit = TimeUnit.MILLISECONDS;

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
	public void destroy() throws BinlogChannelException {

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
		long timeoutMillis = timeUnit.toMillis(timeout);
		long costTimeMillis = costTimeUnit.toMillis(costTime);

		try {
			if (costTimeMillis > timeoutMillis) {
				Thread.sleep(timeoutMillis);
				return null;
			} else {
				Thread.sleep(costTimeMillis);
				return next();
			}
		} catch (InterruptedException e) {
			return null;
		}
	}
}
