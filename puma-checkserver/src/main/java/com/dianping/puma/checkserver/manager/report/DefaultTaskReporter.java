package com.dianping.puma.checkserver.manager.report;

import com.dianping.puma.biz.entity.CheckResultEntity;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckResultService;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.checkserver.model.SourceTargetPair;
import com.dianping.puma.checkserver.model.TaskResult;
import com.dianping.puma.core.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultTaskReporter implements TaskReporter {

    @Autowired
    CheckTaskService checkTaskService;

    @Autowired
    CheckResultService checkResultService;

    @Override
    public void report(CheckTaskEntity checkTask, TaskResult result) {
        try {
            for (SourceTargetPair pair : result.getDifference()) {
                CheckResultEntity entity = new CheckResultEntity();
                entity.setTaskName(checkTask.getTaskName());
                entity.setSourceData(GsonUtil.toJson(pair.getSource()));
                entity.setTargetData(GsonUtil.toJson(pair.getTarget()));
                checkResultService.create(entity);
            }

            checkTask.setCursor(result.getCursor());
            setStatus(checkTask, true, null);
            report0(checkTask);
        } catch (Exception e) {
            report(checkTask, e);
        }
    }

    @Override
    public void report(CheckTaskEntity checkTask, Throwable t) {
        setStatus(checkTask, false, t.getClass().getSimpleName() + ":" + t.getMessage());
        report0(checkTask);
    }

    protected void report0(CheckTaskEntity checkTask) {
        checkTaskService.unlock(checkTask);
    }

    protected void setStatus(CheckTaskEntity checkTask, boolean status, String message) {
        checkTask.setSuccess(status);
        checkTask.setMessage(message);
        checkTask.setOwnerHost(null);
        checkTask.setRunning(false);
        checkTask.setUpdateTime(new Date());
    }
}
