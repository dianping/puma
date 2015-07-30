package com.dianping.puma.syncserver.manager.reporter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskStateReporter implements TaskStateReporter {

	@Override
	public void report() {

	}

	@Scheduled(fixedDelay = 2 * 1000)
	private void scheduledReport() {
		report();
	}
}
