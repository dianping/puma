package com.dianping.puma.monitor.fetcher;

import org.junit.Test;

public class FetcherEventCountMonitorTest {

	FetcherEventCountMonitor fetcherEventCountMonitor = new FetcherEventCountMonitor();

	@Test
	public void test() {
		fetcherEventCountMonitor.record("hello");
	}
}
