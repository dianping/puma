package com.dianping.puma.server.state;

import org.springframework.scheduling.annotation.Scheduled;

public class ScheduledTaskStateReporter implements TaskStateReporter {

	@Override
	public void report() {

	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void scheduledReport() {
		report();
	}
}
