package com.dianping.puma.monitor.fetcher;

import com.dianping.puma.ComponentContainer;
import org.junit.Test;

public class FetcherEventCountMonitorTest {

	FetcherEventCountMonitor fetcherEventCountMonitor = ComponentContainer.SPRING.lookup("fetcherEventCountMonitor");

	@Test
	public void test() {
		fetcherEventCountMonitor.record("hello");
	}
}
