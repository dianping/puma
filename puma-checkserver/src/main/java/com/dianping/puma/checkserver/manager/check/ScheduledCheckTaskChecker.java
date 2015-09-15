package com.dianping.puma.checkserver.manager.check;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.checkserver.manager.dispatch.TaskDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledCheckTaskChecker implements CheckTaskChecker {

	@Autowired
	CheckTaskService checkTaskService;

	@Autowired
	TaskDispatcher taskDispatcher;

	@Override
	public void check() {
		List<CheckTaskEntity> checkTasks = checkTaskService.findAll();
		taskDispatcher.dispatch(checkTasks);
	}

	@Scheduled(fixedDelay = 20 * 1000)
	protected void scheduledCheck() {
		check();
	}
}
