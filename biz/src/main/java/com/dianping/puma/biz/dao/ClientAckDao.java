package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAckEntity;

/**
 * Created by xiaotian.li on 16/3/1.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAckDao {

    void insert(ClientAckEntity entity);

    int update(ClientAckEntity entity);

    int delete(String clientName);
}
