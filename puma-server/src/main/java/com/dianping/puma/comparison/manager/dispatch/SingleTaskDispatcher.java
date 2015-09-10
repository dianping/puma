package com.dianping.puma.comparison.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.TaskResult;
import com.dianping.puma.comparison.manager.lock.TaskLock;
import com.dianping.puma.comparison.manager.lock.TaskLockBuilder;
import com.dianping.puma.comparison.manager.report.TaskReporter;
import com.dianping.puma.comparison.manager.run.TaskRunFutureListener;
import com.dianping.puma.comparison.manager.run.TaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SingleTaskDispatcher implements TaskDispatcher {

	@Autowired
	TaskRunner taskRunner;

	@Autowired
	TaskLockBuilder taskLockBuilder;

	@Autowired
	TaskReporter taskReporter;

	private final long checkTimePeriod = 30 * 60; // 30 min.

	@Override
	public void dispatch(List<CheckTaskEntity> checkTasks) {
		for (final CheckTaskEntity checkTask: checkTasks) {

			final TaskLock localTaskLock = taskLockBuilder.buildLocalLock(checkTask);
			final TaskLock remoteTaskLock = taskLockBuilder.buildRemoteLock(checkTask);

			try {
				if (!localTaskLock.tryLock()) {
					continue;
				}

				if (!remoteTaskLock.tryLock()) {
					localTaskLock.unlock();
					continue;
				}

				if (!setNextTimeIfEnough(checkTask)) {
					continue;
				}

				taskRunner.run(checkTask).addListener(new TaskRunFutureListener() {
					@Override
					public void onSuccess(TaskResult result) {
						taskReporter.report(checkTask, result);
						localTaskLock.unlock();
						remoteTaskLock.unlock();
					}

					@Override
					public void onFailure(Throwable cause) {
						taskReporter.report(checkTask, cause);
						localTaskLock.unlock();
						remoteTaskLock.unlock();
					}
				});

			} catch (Throwable t) {
				localTaskLock.unlock();
				remoteTaskLock.unlock();
			}

			break;
		}
	}

	protected boolean setNextTimeIfEnough(CheckTaskEntity checkTask) {
		Date currTime = checkTask.getCurrTime();
		Date nextTime = new Date(currTime.getTime() + checkTimePeriod);
		if (nextTime.getTime() > new Date().getTime()) {
			return false;
		}
		checkTask.setNextTime(nextTime);
		return true;
	}
}
