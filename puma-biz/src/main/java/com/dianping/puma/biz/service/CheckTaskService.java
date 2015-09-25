package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.model.CheckTaskQueryModel;
import com.dianping.puma.biz.model.PageModel;

import java.util.List;

public interface CheckTaskService {

    CheckTaskEntity findById(int id);

    CheckTaskEntity findByName(String name);

    List<CheckTaskEntity> findRunnable();

    List<CheckTaskEntity> list(CheckTaskQueryModel queryModel,PageModel pageModel);

    int unlock(CheckTaskEntity checkTaskEntity);

    int create(CheckTaskEntity checkTaskEntity);

    int deleteByTaskName(String name);

    boolean tryLock(CheckTaskEntity checkTaskEntity);

    void cleanUp();
}
