package com.dianping.puma.syncserver.executor.load;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

public class LoaderFactory {

	public static Loader createAsyncLoader(DataSource dataSource, ExecutorService sqlExecutorThreadPool) {
		return new AsyncLoader(1, dataSource, sqlExecutorThreadPool);
	}

	public static Loader createConcurrentAsyncLoader(int maxConcurrent, DataSource dataSource,
			ExecutorService sqlExecutorThreadPool) {
		return new AsyncLoader(maxConcurrent, dataSource, sqlExecutorThreadPool);
	}
}
