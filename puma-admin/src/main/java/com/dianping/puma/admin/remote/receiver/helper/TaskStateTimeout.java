package com.dianping.puma.admin.remote.receiver.helper;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class TaskStateTimeout {

	@Autowired
	TaskStateContainer taskStateContainer;

	@Value("30")
	int timeoutSeconds;

	@Scheduled(cron = "0/5 * * * * ?")
	public void timeout() {
		List<TaskState> taskStates = taskStateContainer.getAll();

		for (TaskState taskState: taskStates) {
			Date pre = new Date();
			Date post = DateUtils.addSeconds(taskState.getGmtUpdate(), timeoutSeconds);

			if (pre.after(post)) {
				taskState.setStatus(Status.DISCONNECTED);
			}
		}
	}
}
