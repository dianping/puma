package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAdditionEntity;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAdditionDao {

    void insert(ClientAdditionEntity entity);

    int update(ClientAdditionEntity entity);

    int delete(String clientName);
}
