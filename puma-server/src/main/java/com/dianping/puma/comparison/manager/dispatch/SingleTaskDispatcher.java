package com.dianping.puma.comparison.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;
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
	TaskRunner taskRunner;

	@Override
	public void dispatch(List<CheckTaskEntity> checkTasks) {
		for (CheckTaskEntity checkTask: checkTasks) {
			final int taskId = checkTask.getId();

			// Query the local and remote lock.
			final TaskLock localTaskLock = TaskLockFactory.getNoReentranceTaskLock(taskId);
			final TaskLock remoteTaskLock = TaskLockFactory.getDatabaseTaskLock(taskId);

			try {
				if (!localTaskLock.tryLock()) {
					continue;
				}

				if (!remoteTaskLock.tryLock()) {
					localTaskLock.unlock();
					continue;
				}

				taskRunner.run(checkTask).addListener(new TaskRunFutureListener() {

					protected void unlock() {
						localTaskLock.unlock();
						remoteTaskLock.unlock();
					}

					@Override
					public void onSuccess(Void result) {
						unlock();
					}

					@Override
					public void onFailure(Throwable cause) {
						unlock();
					}
				});

			} catch (Throwable t) {
				localTaskLock.unlock();
				remoteTaskLock.unlock();
			}
		}
	}
}
