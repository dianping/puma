package com.dianping.puma.syncserver.executor.load;

import java.util.concurrent.ExecutorService;

public class LoaderFactory {

	public static Loader createAsyncLoader(ExecutorService es) {
		return new AsyncLoader(1, es);
	}

	public static Loader createConcurrentAsyncLoader(int maxConcurrent, ExecutorService es) {
		return new AsyncLoader(maxConcurrent, es);
	}
}
