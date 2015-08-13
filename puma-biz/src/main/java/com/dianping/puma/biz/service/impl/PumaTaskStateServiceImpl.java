package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaTaskStateDao;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.biz.service.PumaTaskStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class PumaTaskStateServiceImpl implements PumaTaskStateService {

    @Autowired
    PumaTaskStateDao pumaTaskStateDao;

    @Override
    public List<PumaTaskStateEntity> find(String taskName) {
        List<PumaTaskStateEntity> taskStates = pumaTaskStateDao.findByTaskName(taskName);
        return loadFullPumaTaskStates(taskStates);
    }

    @Override
    public PumaTaskStateEntity findByTaskNameAndServerName(String taskName, String serverName) {
        PumaTaskStateEntity taskState = pumaTaskStateDao.findByTaskNameAndServerName(taskName, serverName);
        return loadFullPumaTaskState(taskState);
    }

    @Override
    public void createOrUpdate(PumaTaskStateEntity taskState) {
        taskState.setUpdateTime(new Date());

        if (pumaTaskStateDao.findByTaskNameAndServerName(taskState.getTaskName(), taskState.getServerName()) == null) {
            pumaTaskStateDao.insert(taskState);
        } else {
            pumaTaskStateDao.update(taskState);
        }
    }

    protected PumaTaskStateEntity loadFullPumaTaskState(PumaTaskStateEntity taskState) {
        return taskState;
    }

    protected List<PumaTaskStateEntity> loadFullPumaTaskStates(List<PumaTaskStateEntity> taskStates) {
        for (PumaTaskStateEntity taskState : taskStates) {
            loadFullPumaTaskState(taskState);
        }
        return taskStates;
    }
}
