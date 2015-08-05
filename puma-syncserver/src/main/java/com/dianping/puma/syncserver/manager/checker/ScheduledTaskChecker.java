package com.dianping.puma.syncserver.manager.checker;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskChecker implements TaskChecker {

	@Override
	public void check() {

	}

	@Scheduled(fixedDelay = 2 * 1000)
	private void scheduledCheck() {
		check();
	}
}
