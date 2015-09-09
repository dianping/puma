package com.dianping.puma.comparison.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.manager.container.TaskContainer;
import com.dianping.puma.comparison.manager.lock.TaskLock;
import com.dianping.puma.comparison.manager.lock.TaskLockFactory;
import com.dianping.puma.comparison.manager.run.TaskRunFutureListener;
import com.dianping.puma.comparison.manager.run.TaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SingleTaskDispatcher implements TaskDispatcher {

	@Autowired
	TaskContainer taskContainer;

	@Autowired
	TaskRunner taskRunner;

	@Override
	public void dispatch(List<CheckTaskEntity> checkTasks) {
		for (CheckTaskEntity checkTask: checkTasks) {
			int taskId = checkTask.getId();

			// Local lock.
			if (taskContainer.contains(taskId)) {
				continue;
			}

			// Remote lock.
			TaskLock taskLock = TaskLockFactory.getDatabaseTaskLock(taskId);
			if (!taskLock.tryLock()) {
				continue;
			}

			taskContainer.create(checkTask);
			taskRunner.run(checkTask).addListener(new TaskRunFutureListener() {
				@Override
				public void onSuccess(Void result) {

				}

				@Override
				public void onFailure(Throwable cause) {

				}
			});

		}
	}
}
