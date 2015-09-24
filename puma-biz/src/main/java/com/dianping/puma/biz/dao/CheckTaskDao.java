package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.model.CheckTaskQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CheckTaskDao {

    CheckTaskEntity findById(int id);

    List<CheckTaskEntity> findRunnable();

    int count(@Param(value = "model") CheckTaskQueryModel model);

    List<CheckTaskEntity> list(@Param(value = "model") CheckTaskQueryModel model,
                               @Param(value = "offset") int offset,
                               @Param(value = "limit") int limit);

    int countByResult(@Param(value = "model") CheckTaskQueryModel model);

    List<CheckTaskEntity> listByResult(@Param(value = "model") CheckTaskQueryModel model,
                               @Param(value = "offset") int offset,
                               @Param(value = "limit") int limit);

    int create(CheckTaskEntity checkTaskEntity);

    void cleanUp();

    int unlock(CheckTaskEntity checkTaskEntity);

    int tryLock(CheckTaskEntity checkTaskEntity);

    int deleteByTaskName(String taskName);

}
