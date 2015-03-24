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
import java.util.Map;

@Service
public class TaskStateCleaner {

	@Autowired
	TaskStateContainer taskStateContainer;

	@Value("60")
	int cleanSeconds;

	@Scheduled(cron = "0/5 * * * * ?")
	public void clean() {
		for (Iterator<Map.Entry<String, TaskState>> it = taskStateContainer.getAll().entrySet().iterator(); it.hasNext();) {
			TaskState taskState = it.next().getValue();

			Date pre = new Date();
			Date post = DateUtils.addSeconds(taskState.getGmtUpdate(), cleanSeconds);

			if (pre.after(post)) {
				it.remove();
			}
		}
	}
}
