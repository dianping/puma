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
    public int unlock(CheckTaskEntity checkTaskEntity) {
        return checkTaskDao.unlock(checkTaskEntity);
    }

    @Override
    public int create(CheckTaskEntity checkTaskEntity) {
        return checkTaskDao.create(checkTaskEntity);
    }

    @Override
    public int deleteByTaskName(String name) {
        return checkTaskDao.deleteByTaskName(name);
    }

    @Override
    public boolean tryLock(CheckTaskEntity checkTaskEntity) {
        return checkTaskDao.tryLock(checkTaskEntity) > 0;
    }

    public void cleanUp(){
        checkTaskDao.cleanUp();
    }
}
