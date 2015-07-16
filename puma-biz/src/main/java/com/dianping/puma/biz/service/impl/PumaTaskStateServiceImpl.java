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
        return pumaTaskStateDao.findByTaskName(taskName);
    }

    @Override
    public PumaTaskStateEntity findByTaskNameAndServerName(String taskName, String serverName) {
        return pumaTaskStateDao.findByTaskNameAndServerName(taskName, serverName);
    }

    @Override
    public void createOrUpdate(PumaTaskStateEntity taskState) {
        taskState.setGmtUpdate(new Date());

        if (pumaTaskStateDao.find(taskState.getId()) == null) {
            pumaTaskStateDao.insert(taskState);
        } else {
            pumaTaskStateDao.update(taskState);
        }
    }
}
