package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;

import java.util.List;
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
	public void init(
			String targetName,
			long dbServerId,
			long sc,
			BinlogInfo binlogInfo,
			long timestamp,
			String database,
			List<String> tables,
			boolean dml,
			boolean ddl,
			boolean transaction) throws BinlogChannelException {
	}

	@Override
	public void destroy() throws BinlogChannelException {

	}

	@Override
	public Event next() throws BinlogChannelException {
		ChangedEvent event = new RowChangedEvent();
		event.setDatabase(constant.getDatabase());
		event.setTable(constant.getTable());
		return event;
	}

	@Override
	public Event next(long timeout, TimeUnit timeUnit) throws BinlogChannelException {
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
