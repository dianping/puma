package com.dianping.puma.storage.manage.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.manage.WriteManager;

import java.io.IOException;

public class DefaultWriteManager extends AbstractLifeCycle implements WriteManager {

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException {

	}
}
