package com.dianping.puma.syncserver.reporter;

import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.scheduling.annotation.Scheduled;

/*
@Service
public class StatusReporter {
	@Autowired
	private SwallowEventPublisher statusEventPublisher;
	@Autowired
	private SystemStatusContainer systemStatusContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
		if (event.getStatusList() != null && event.getStatusList().size() > 0) {
			statusEventPublisher.publish(event);
		}
	}

}

public class SyncTaskStateReporter {

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {

	}
}*/
