package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.ChangedEvent;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

public class AsyncConcurrentLoader extends AbstractLoader {

	protected DataSource dataSource;

	protected ExecutorService executorService;

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public LoadFuture load(ChangedEvent binlogEvent) {
		return null;
	}
}
