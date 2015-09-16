package com.dianping.puma.checkserver.manager.report;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.checkserver.model.TaskResult;
import com.dianping.puma.core.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultTaskReporter implements TaskReporter {

    private final Logger logger = LoggerFactory.getLogger(DefaultTaskReporter.class);

    @Autowired
    CheckTaskService checkTaskService;

    @Override
    public void report(CheckTaskEntity checkTask, TaskResult result) {
        setCurrTime(checkTask, checkTask.getNextTime());

        if (result.getDifference().size() == 0) {
            setStatus(checkTask, true, null);
        } else {
            logger.error("Check Difference: \n{}.", GsonUtil.toJson(result.getDifference()));
            setStatus(checkTask, true, "Check Difference");
        }

        report0(checkTask);
    }

    @Override
    public void report(CheckTaskEntity checkTask, Throwable t) {
        setStatus(checkTask, false, t.getClass().getSimpleName() + ":" + t.getMessage());

        report0(checkTask);
    }

    protected void report0(CheckTaskEntity checkTask) {
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
