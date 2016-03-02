package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientConnectEntity;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientConnectDao {

    void insert(ClientConnectEntity entity);

    int update(ClientConnectEntity entity);

    int delete(String clientName);
}
