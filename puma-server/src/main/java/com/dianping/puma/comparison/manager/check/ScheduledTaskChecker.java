package com.dianping.puma.comparison.manager.check;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskChecker implements TaskChecker {

	@Override
	public void check() {

	}

	@Scheduled(fixedDelay = 60 * 1000)
	protected void scheduledCheck() {
		check();
	}
}
