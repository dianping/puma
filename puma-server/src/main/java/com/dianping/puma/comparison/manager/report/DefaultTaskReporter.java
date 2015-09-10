package com.dianping.puma.comparison.manager.report;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.comparison.model.TaskResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultTaskReporter implements TaskReporter {

	@Autowired
	CheckTaskService checkTaskService;

	@Override
	public void report(CheckTaskEntity checkTask, TaskResult result) {
		setCurrTime(checkTask, checkTask.getNextTime());

		if (result.getDifference().size() == 0) {
			setStatus(checkTask, true, null);
		} else {
			setStatus(checkTask, false, result.getDifference().toString());
		}

		report0(checkTask);
	}

	@Override
	public void report(CheckTaskEntity checkTask, Throwable t) {
		setStatus(checkTask, false, ExceptionUtils.getStackTrace(t));

		report0(checkTask);
	}

	public void report0(CheckTaskEntity checkTask) {
		checkTaskService.update(checkTask);
	}

	protected void setCurrTime(CheckTaskEntity checkTask, Date nextTime) {
		checkTask.setCurrTime(nextTime);
	}

	protected void setStatus(CheckTaskEntity checkTask, boolean status, String message) {
		checkTask.setSuccess(status);
		checkTask.setMessage(message);
	}
}
