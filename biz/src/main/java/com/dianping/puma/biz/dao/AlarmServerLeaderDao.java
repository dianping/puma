package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.AlarmServerLeaderEntity;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface AlarmServerLeaderDao {

    AlarmServerLeaderEntity find(boolean leader);

    void insert(AlarmServerLeaderEntity entity);

    int update(AlarmServerLeaderEntity entity);

    int delete(AlarmServerLeaderEntity entity);
}
