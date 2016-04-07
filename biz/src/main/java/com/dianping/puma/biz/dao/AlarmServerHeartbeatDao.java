package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.AlarmServerHeartbeatEntity;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface AlarmServerHeartbeatDao {

    List<AlarmServerHeartbeatEntity> findAll();

    AlarmServerHeartbeatEntity find(String host);

    void insert(AlarmServerHeartbeatEntity entity);

    int update(AlarmServerHeartbeatEntity entity);

    int delete(String host);
}
