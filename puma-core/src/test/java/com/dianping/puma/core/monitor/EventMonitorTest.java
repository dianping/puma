package com.dianping.puma.core.monitor;

import com.dianping.puma.core.MockTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class EventMonitorTest extends MockTest {

	EventMonitor eventMonitor = new EventMonitor();

	@Mock
	Monitor monitor;

	@Before
	public void before() {
		eventMonitor.stop();
		eventMonitor.setType("type");
		eventMonitor.setCountThreshold(1000L);
		eventMonitor.setMonitor(monitor);
		eventMonitor.start();
	}

	@Test
	public void testRecord() {
		// Case 1: Count = 1.
		for (int i = 0; i != 1; ++i) {
			eventMonitor.record("name0", "0");
		}
		verify(monitor, times(0)).logEvent("type", "name0", "0", "");

		// Case 2: Count = 999.
		for (int i = 0; i != 999; ++i) {
			eventMonitor.record("name1", "0");
		}
		verify(monitor, times(0)).logEvent("type", "name1", "0", "");

		// Case 3: Count = 1000.
		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name2", "0");
		}
		verify(monitor, times(1)).logEvent("type", "name2", "0", "");

		// Case 4: Count = 8888.
		for (int i = 0; i != 8888; ++i) {
			eventMonitor.record("name3", "0");
		}
		verify(monitor, times(8)).logEvent("type", "name3", "0", "");

		// Case 5: Keep on.
		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name3", "0");
		}
		verify(monitor, times(9)).logEvent("type", "name3", "0", "");
	}

	@Test
	public void testControl() {
		// After starting the event monitor.
		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name0", "0");
		}
		verify(monitor, times(1)).logEvent("type", "name0", "0", "");

		eventMonitor.stop();

		// After stopping the event monitor.
		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name1", "0");
		}
		verify(monitor, times(0)).logEvent("type", "name1", "0", "");

		eventMonitor.start();

		// After starting the event monitor.
		for (int i = 0; i != 999; ++i) {
			eventMonitor.record("name0", "0");
		}
		verify(monitor, times(1)).logEvent("type", "name0", "0", "");

		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name1", "0");
		}
		verify(monitor, times(1)).logEvent("type", "name1", "0", "");

		eventMonitor.pause();

		// After pausing the event monitor.
		for (int i = 0; i != 1000; ++i) {
			eventMonitor.record("name0", "0");
		}
		verify(monitor, times(1)).logEvent("type", "name0", "0", "");

		eventMonitor.start();

		// After starting the event monitor.
		for (int i = 0; i != 1; ++i) {
			eventMonitor.record("name0", "0");
		}
		verify(monitor, times(2)).logEvent("type", "name0", "0", "");
	}
}
