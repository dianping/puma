package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.CheckTaskDao;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CheckTaskServiceImpl implements CheckTaskService {

    @Autowired
    CheckTaskDao checkTaskDao;

    @Override
    public CheckTaskEntity findById(int id) {
        return checkTaskDao.findById(id);
    }

    @Override
    public List<CheckTaskEntity> findRunnable() {
        return checkTaskDao.findRunnable();
    }

    @Override
    public int update(CheckTaskEntity checkTaskEntity) {
        checkTaskEntity.setUpdateTime(new Date());
        return checkTaskDao.update(checkTaskEntity);
    }

    @Override
    public int create(CheckTaskEntity checkTaskEntity) {
        return checkTaskDao.create(checkTaskEntity);
    }

    @Override
    public int deleteByTaskName(String name) {
        return checkTaskDao.deleteByTaskName(name);
    }
}
