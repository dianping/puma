package com.dianping.puma.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

@Service
public class ServerTaskStatusPublisher {

	@Autowired
	private SwallowEventPublisher statusEventPublisher;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {

	}
}
