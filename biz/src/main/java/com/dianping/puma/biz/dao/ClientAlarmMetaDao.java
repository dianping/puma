package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmMetaEntity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmMetaDao {

    ClientAlarmMetaEntity find(String clientName);

    void insert(ClientAlarmMetaEntity entity);

    int update(ClientAlarmMetaEntity entity);

    void delete(String clientName);
}
